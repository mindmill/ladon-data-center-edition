/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.impl

import com.google.common.base.Strings
import de.mc.ladon.server.core.api.exceptions.LadonIllegalArgumentException
import de.mc.ladon.server.core.api.exceptions.LadonObjectNotFoundException
import de.mc.ladon.server.core.api.hooks.LadonHookManager
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.entities.ChangeType
import de.mc.ladon.server.core.api.persistence.entities.HistoryKey
import de.mc.ladon.server.core.api.persistence.entities.Metadata
import de.mc.ladon.server.core.api.persistence.entities.ResourceKey
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.persistence.entities.impl.LadonContentMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonMetadata
import de.mc.ladon.server.core.persistence.entities.impl.LadonPropertyMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonResourceKey
import de.mc.ladon.server.persistence.cassandra.dao.api.ObjectDataAccessor
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbObjectData
import java.util.*
import javax.inject.Inject
import javax.inject.Named


/**
 * DAO for object data access
 * @author ralf ulrich on 30.01.15.
 */
@Named
open class MetadataDAOImpl
@Inject constructor(mm: MappingManagerProvider,
                    val hookManager: LadonHookManager) : MetadataDAO {


    // private val LOG = LoggerFactory.getLogger(javaClass)

    private val filingMapper: (DbObjectData) -> Metadata = { fe ->
        LadonMetadata().apply {
            set(LadonContentMeta(fe.streamid!!, fe.md5!!, fe.length!!, fe.created!!, fe.createdBy!!, fe.deleted, fe.deletedBy))
            set(LadonResourceKey(fe.repoId!!, fe.versionseriesId!!, fe.changeToken!!))
            set(LadonPropertyMeta(fe.meta ?: hashMapOf()))
        }
    }

    val objectMapper = mm.getMapper(DbObjectData::class.java)
    val accessor = mm.getAccessor((ObjectDataAccessor::class.java))


    override fun getMetadataHistory(cc: LadonCallContext, key: HistoryKey): List<Metadata> {
        return accessor.value.getObjectVersions(key.repositoryId, key.versionSeriesId).filterNotNull().map(filingMapper)
    }

    override fun getMetadataLatest(cc: LadonCallContext, key: HistoryKey): Metadata? {
        return accessor.value.getObject(key.repositoryId, key.versionSeriesId)?.let(filingMapper)
    }

    override fun getMetadata(cc: LadonCallContext, key: ResourceKey): Metadata? {
        return getDbObjectData(key)?.let(filingMapper)
    }

    override fun saveMetadata(cc: LadonCallContext, key: ResourceKey, obj: Metadata): ResourceKey {
        hookManager.getChangeObjectDataHooks().forEach {
            it.onBeforeCreateObject(key, obj)
        }
        val newKey = putObject(cc, key, obj)
        hookManager.getChangeObjectDataHooks().forEach {
            it.onAfterCreateObject(key, obj)
        }
        return newKey
    }

    private fun putObject(cc: LadonCallContext, key: ResourceKey, obj: Metadata): ResourceKey {
        val newKey = key.updatedKey(cc)
        val content = obj.content()
        val props = obj.properties()
        objectMapper.value.save(
                DbObjectData(
                        newKey.repositoryId,
                        newKey.versionSeriesId,
                        newKey.changeToken,
                        ChangeType.PUT.toString(),
                        props.content,
                        content.id,
                        content.length,
                        content.hash,
                        content.created,
                        content.createdBy))
        return newKey
    }


    override fun deleteMetadata(cc: LadonCallContext, key: ResourceKey) {
        val data = getDbObjectData(key)
                ?: throw LadonObjectNotFoundException("couldn't delete object with key $key , not found")
        val newKey = key.updatedKey(cc)
        data.deleted = Date()
        data.deletedBy = cc.getUser().name
        data.changeToken = newKey.changeToken
        data.operation = ChangeType.DELETE.toString()
        objectMapper.value.save(data)
        hookManager.getChangeObjectDataHooks().forEach {
            it.onAfterDeleteObject(key, data.let(filingMapper))
        }
    }

    private fun getDbObjectData(key: ResourceKey) = accessor.value.getObjectVersion(key.repositoryId, key.versionSeriesId, key.changeToken)

    override fun deleteMetadataHistory(cc: LadonCallContext, key: HistoryKey) {
        val metadataVersions = getMetadataHistory(cc, key)
        metadataVersions.forEach { meta ->
            deleteMetadata(cc, meta.key())
        }
    }

    override fun removeMetadata(cc: LadonCallContext, key: ResourceKey) {
        //getMetadata(cc, key) ?: throw LadonStorageException("couldn't delete object $key , not found")
        accessor.value.deleteObjectVersion(key.repositoryId, key.versionSeriesId, key.changeToken)
    }

    override fun listAllMetadata(cc: LadonCallContext,
                                 repoId: String,
                                 prefix: String,
                                 marker: String?,
                                 delimiter: String?,
                                 limit: Int,
                                 includeVersions: Boolean): Pair<Pair<List<Metadata>, List<String>>, Boolean> {
        val uniqueKeys = hashSetOf<String>()
        val startPoint = Strings.emptyToNull(marker) ?: prefix
        if (delimiter != null && delimiter != "/") throw LadonIllegalArgumentException("delimiter other than / are not supported")

        val rsIterator = accessor.value.getObjectsStartingAt(repoId, startPoint).iterator()
        // in case of marker start listing with the next entry
        if (!Strings.isNullOrEmpty(marker) && rsIterator.hasNext()) rsIterator.next()

        val commonPrefixes = hashSetOf<String>()
        val result = mutableListOf<Metadata>()
        var counter = 0
        for (objectData in rsIterator) {
            val currentKey = objectData.versionseriesId!!
            if (includeVersions || !uniqueKeys.contains(currentKey)) {
                if (!includeVersions) {
                    uniqueKeys.add(currentKey)
                    if (objectData.deleted != null) continue
                }
                // stop when keys don't start with the prefix anymore
                if (currentKey.startsWith(prefix)) {
                    counter++
                    // try one more
                    if (counter > limit) break
                    if (delimiter != null) {
                        val pref = getCommonPrefix(currentKey, prefix)
                        if (pref == null) {
                            // if (!currentKey.endsWith(prefix)) {
                            result.add(objectData.let(filingMapper))
                            //}
                        } else {
                            val added = commonPrefixes.add(pref)
                            // common prefix counts as 1 for all keys
                            if (!added) counter--
                        }
                    } else {
                        result.add(objectData.let(filingMapper))
                    }
                } else
                    break
            }
        }
        // found more than requested
        val hasMore = counter > limit && limit != 0
        return Pair(result to commonPrefixes.toList(), hasMore)
    }

    private fun getCommonPrefix(key: String, prefix: String, delimiter: String = "/"): String? {
        val pos = prefix.length
        if (!key.startsWith(prefix)) return null
        val rest = key.substring(pos)
        val right = rest.indexOf(delimiter)
        if (right == -1) return null
        val left = prefix.lastIndexOf(delimiter)
        return if (left == -1) {
            key.substring(0, right + pos)
        } else {
            key.substring(left + 1, right + pos)
        }

    }
}