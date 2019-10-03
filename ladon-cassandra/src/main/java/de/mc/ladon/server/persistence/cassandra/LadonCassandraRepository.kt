/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra

import com.datastax.driver.core.utils.UUIDs
import de.mc.ladon.server.core.api.*
import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.api.persistence.dao.UserRoleDAO
import de.mc.ladon.server.core.api.persistence.entities.Metadata
import de.mc.ladon.server.core.api.persistence.entities.Repository
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.persistence.entities.impl.*
import de.mc.ladon.server.persistence.cassandra.tasks.CleanupOldVersionsTaskRunner
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * on 3.10.19.
 */
@Named
open class LadonCassandraRepository @Inject constructor(
        val repoDAO: RepositoryDAO,
        val metaDAO: MetadataDAO,
        val userRoleDAO: UserRoleDAO,
        val binaryDataDAO: BinaryDataDAO,
        val versionsCleaner: CleanupOldVersionsTaskRunner) : LadonRepository {

    data class UserCallContext(val userId: String) : LadonCallContext {
        val changeToken = UUIDs.timeBased()
        override fun getUser() = LadonUser(userId)
        override fun getObjectId() = NOTUSED("not implemented")
        override fun setObjectId(objId: String) = NOTUSED("not implemented")
        override fun getPath() = NOTUSED("not implemented")
        override fun getRepositoryId() = NOTUSED("not implemented")
        override fun getCallId() = LadonCallId(changeToken)
    }

    val contentType = "Content-Type"
    private fun Metadata.getContentType() =
            properties()[contentType].orEmpty()

    private fun Date.toLocalDate() = LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
    fun getCC(userId: String): UserCallContext {
        checkUserExists(userId)
        return UserCallContext(userId)
    }

    fun checkUserExists(userId: String) = userRoleDAO.getUser(userId)
            ?: throw UserNotFound(userId)

    override fun listBuckets(userId: String): List<String> {
        return repoDAO.getRepositories(getCC(userId)).map { it.repoId!! }.toList()
    }

    override fun createNewBucket(userId: String,
                                 bucket: String) {
        val cc = getCC(userId)
        repoDAO.getRepository(cc, bucket)?.let { throw BucketAlreadyExists(bucket) }
        repoDAO.addRepository(cc, bucket)
    }

    override fun deleteEmptyBucket(userId: String,
                                   bucket: String) {
        withUserAndBucket(userId, bucket) {
            if (metaDAO.listAllMetadata(cc, bucket, "", "", null, 1, true).first.first.isEmpty()) {
                repoDAO.deleteRepository(cc, bucket)
            } else {
                throw BucketNotEmpty(bucket)
            }
        }
    }

    override fun deleteDocument(userId: String,
                                bucket: String,
                                key: String): Document {
        return withUserAndBucket(userId, bucket) {

            val meta = metaDAO.getMetadataLatest(cc, LadonHistoryKey(bucket, key))
                    ?: throw DocumentNotFound(bucket, key)
            metaDAO.deleteMetadata(cc, meta.key())
            if (repo.versioned != true) {
                versionsCleaner.cleanupVersions(cc, bucket, key)
            }
            val contentMeta = meta.content()
            Document(bucket, key, meta.key().changeToken.toString(), true, contentMeta.length, contentMeta.hash,
                    contentMeta.created.toLocalDate(), meta.getContentType(), meta.properties().content)
        }

    }


    override fun deleteDocumentVersion(userId: String,
                                       bucket: String,
                                       key: String,
                                       version: String): Document {
        return withUserAndBucket(userId, bucket) {
            val meta = metaDAO.getMetadata(cc, LadonResourceKey(bucket, key, UUID.fromString(version)))
                    ?: throw DocumentNotFound(bucket, key)
            metaDAO.deleteMetadata(cc, meta.key())
            val contentMeta = meta.content()
            Document(bucket,
                    key,
                    meta.key().changeToken.toString(),
                    true,
                    contentMeta.length,
                    contentMeta.hash,
                    contentMeta.created.toLocalDate(),
                    meta.getContentType(),
                    meta.properties().content)
        }
    }

    override fun getDocument(userId: String,
                             bucket: String,
                             key: String): ContentDocument {
        return withUserAndBucket(userId, bucket) {

            val meta = metaDAO.getMetadataLatest(cc, LadonHistoryKey(bucket, key))
                    ?: throw DocumentNotFound(bucket, key)

            if (meta.isDeleted()) throw DocumentNotFound(bucket, key)
            val contentMeta = meta.content()
            val resultMeta =
                    Document(bucket,
                            key,
                            meta.key().changeToken.toString(),
                            true, contentMeta.length,
                            contentMeta.hash,
                            contentMeta.created.toLocalDate(),
                            meta.getContentType(),
                            meta.properties().content)

            ContentDocument(resultMeta,
                    binaryDataDAO.getContentStream(cc, bucket, contentMeta.id, null, null)
                            ?: ByteArrayInputStream(ByteArray(0)))
        }
    }

    override fun getDocumentVersion(userId: String,
                                    bucket: String,
                                    key: String,
                                    version: String): ContentDocument {
        return withUserAndBucket(userId, bucket) {

            val meta = metaDAO.getMetadata(cc, LadonResourceKey(bucket, key, UUID.fromString(version)))
                    ?: throw DocumentNotFound(bucket, key)
            val metaLatest = metaDAO.getMetadataLatest(cc, LadonHistoryKey(bucket, key))

            val contentMeta = meta.content()
            val resultMeta =
                    Document(bucket,
                            key,
                            version,
                            meta.key() == metaLatest?.key(),
                            contentMeta.length,
                            contentMeta.hash,
                            contentMeta.created.toLocalDate(),
                            meta.getContentType(),
                            meta.properties().content)

            ContentDocument(resultMeta,
                    binaryDataDAO.getContentStream(cc, bucket, contentMeta.id, null, null)
                            ?: ByteArrayInputStream(ByteArray(0)))
        }
    }

    override fun getDocumentVersionMetadata(userId: String,
                                            bucket: String,
                                            key: String,
                                            version: String): Document {
        return getDocumentVersion(userId, bucket, key, version).meta
    }

    override fun getMetadata(userId: String,
                             bucket: String,
                             key: String): Document {
        return getDocument(userId, bucket, key).meta
    }

    override fun listDocumentVersions(userId: String,
                                      bucket: String,
                                      key: String): List<Document> {
        return withUserAndBucket(userId, bucket) {
            metaDAO.getMetadataHistory(cc, LadonHistoryKey(bucket, key)).mapIndexed { index, metadata ->
                val contentMeta = metadata.content()
                Document(bucket,
                        key,
                        metadata.key().changeToken.toString(),
                        index == 0,
                        contentMeta.length,
                        contentMeta.hash,
                        contentMeta.created.toLocalDate(),
                        metadata.getContentType(),
                        metadata.properties().content)
            }
        }


    }

    override fun listDocuments(userId: String,
                               bucket: String,
                               prefix: String?,
                               marker: String?,
                               delimiter: String?,
                               limit: Long?): List<Document> {
        return withUserAndBucket(userId, bucket) {

            val result = metaDAO.listAllMetadata(cc, bucket, prefix.orEmpty(), marker, delimiter, (limit
                    ?: 1000).toInt(), false)
            val (objectList, prefixes) = result.first
            val truncated = result.second
            objectList.map {
                val content = it.content()
                Document(
                        it.key().repositoryId,
                        it.key().versionSeriesId,
                        it.key().changeToken.toString(),
                        true,
                        content.length,
                        it.content().hash,
                        it.content().created.toLocalDate(),
                        it.getContentType(),
                        it.properties().content)
            }
        }
    }

    override fun putDocument(userId: String,
                             bucket: String,
                             key: String,
                             content: InputStream,
                             metadata: Map<String, String>): Document {
        return withUserAndBucket(userId, bucket) {

            content.use {
                val info = binaryDataDAO.saveContentStream(cc, bucket, it)
                val hexMd5 = info.md5.drop(2).toUpperCase()
                val lrk = LadonResourceKey(bucket, key, cc.getCallId().id())
                val meta = LadonMetadata()
                val props = LadonPropertyMeta(metadata.toMutableMap().apply { put("etag", hexMd5) })
                meta.set(props)
                // val name = objectKey.split("/").filterNotNull().last()
                val created = Date()
                meta.set(LadonContentMeta(info.id, hexMd5, info.length.toLong(), created, userId))
                metaDAO.saveMetadata(cc, lrk, meta)
                Document(
                        bucket,
                        key,
                        cc.changeToken.toString(),
                        true,
                        info.length.longValueExact(),
                        info.md5,
                        created.toLocalDate(),
                        meta.getContentType(),
                        props.content)

            }
        }
    }

    override fun updateDocumentMetadata(userId: String,
                                        bucket: String,
                                        key: String,
                                        version: String,
                                        metadata: Map<String, String>): Document {
        return withUserAndBucket(userId, bucket) {
            val lrk = LadonResourceKey(bucket, key, UUID.fromString(version))
            val metadataCurrent = metaDAO.getMetadata(cc, lrk)
                    ?: throw DocumentNotFound(bucket, key)
            metadataCurrent.properties().content.putAll(metadata)
            val newKey = metaDAO.saveMetadata(cc, lrk, metadataCurrent)
            val contentMeta = metadataCurrent.content()
            Document(bucket,
                    key,
                    newKey.changeToken.toString(),
                    true,
                    contentMeta.length,
                    contentMeta.hash,
                    contentMeta.created.toLocalDate(),
                    metadataCurrent.getContentType(),
                    metadataCurrent.properties().content)
        }
    }

    override fun copyDocument(userId: String,
                              bucket: String,
                              key: String,
                              targetBucket: String,
                              targetKey: String): Document {
        return withUserAndBucket(userId, bucket) {
            repoDAO.getRepository(cc, targetBucket) ?: throw BucketNotFound(targetBucket)
            val meta = metaDAO.getMetadataLatest(cc, LadonHistoryKey(bucket, key))
                    ?: throw DocumentNotFound(bucket, key)
            val sourceContentMeta = meta.content() as LadonContentMeta
            val newStreamId = binaryDataDAO.copyContentStream(cc, bucket, sourceContentMeta.id, targetBucket)
            val newContent = sourceContentMeta.copy(id = newStreamId, created = Date(), createdBy = userId)
            val destKey = LadonResourceKey(targetBucket, targetKey, cc.changeToken)
            meta.set(newContent)
            val newKey = metaDAO.saveMetadata(cc, destKey, meta)
            Document(bucket,
                    key,
                    newKey.changeToken.toString(),
                    true,
                    newContent.length,
                    newContent.hash,
                    newContent.created.toLocalDate(),
                    meta.getContentType(),
                    meta.properties().content)
        }
    }


    private class UserAndBucketCtx(val userId: String,
                                   val bucket: String,
                                   val cc: UserCallContext,
                                   val repo: Repository)

    private fun <T> withUserAndBucket(userId: String,
                                      bucket: String,
                                      body: UserAndBucketCtx.() -> T): T {
        val cc = getCC(userId)
        val repo = repoDAO.getRepository(cc, bucket) ?: throw BucketNotFound(bucket)
        return body(UserAndBucketCtx(userId, bucket, cc, repo))
    }

}

inline fun NOTUSED(reason: String): Nothing = throw NotImplementedError("Don't use this : $reason")
