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
 * on 21.10.16.
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
        override fun getObjectId() = TODO("not implemented")
        override fun setObjectId(objId: String) = TODO("not implemented")
        override fun getPath() = TODO("not implemented")
        override fun getRepositoryId() = TODO("not implemented")
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

    override fun createNewBucket(userId: String, bucket: String) {
        val cc = getCC(userId)
        repoDAO.getRepository(cc, bucket)?.let { throw BucketAlreadyExists(bucket) }
        repoDAO.addRepository(cc, bucket)
    }

    override fun deleteEmptyBucket(userId: String, bucket: String) {
        val cc = getCC(userId)
        repoDAO.getRepository(cc, bucket) ?: throw BucketNotFound(bucket)
        if (metaDAO.listAllMetadata(cc, bucket, "", "", null, 1, true).first.first.isEmpty()) {
            repoDAO.deleteRepository(cc, bucket)
        } else {
            throw BucketNotEmpty(bucket)
        }
    }

    override fun deleteDocument(userId: String, bucket: String, key: String): Document {
        val lcc = getCC(userId)
        val repo = repoDAO.getRepository(lcc, bucket) ?: throw BucketNotFound(bucket)
        val meta = metaDAO.getMetadataLatest(lcc, LadonHistoryKey(bucket, key)) ?: throw DocumentNotFound(bucket, key)
        metaDAO.deleteMetadata(lcc, meta.key())
        if (repo.versioned != true) {
            versionsCleaner.cleanupVersions(lcc, bucket, key)
        }
        val contentMeta = meta.content()
        return Document(bucket, key, meta.key().changeToken.toString(), true, contentMeta.length, contentMeta.hash,
                contentMeta.created.toLocalDate(), meta.getContentType(), meta.properties().content)
    }


    override fun deleteDocumentVersion(userId: String, bucket: String, key: String, version: String): Document {
        val lcc = getCC(userId)
        repoDAO.getRepository(lcc, bucket) ?: throw BucketNotFound(bucket)
        val meta = metaDAO.getMetadata(lcc, LadonResourceKey(bucket, key, UUID.fromString(version)))
                ?: throw DocumentNotFound(bucket, key)
        metaDAO.deleteMetadata(lcc, meta.key())
        val contentMeta = meta.content()
        return Document(bucket, key, meta.key().changeToken.toString(), true, contentMeta.length, contentMeta.hash,
                LocalDateTime.ofInstant(contentMeta.created.toInstant(), ZoneId.systemDefault()), meta.getContentType(), meta.properties().content)
    }

    override fun getDocument(userId: String, bucket: String, key: String): ContentDocument {
        val cc = getCC(userId)
        repoDAO.getRepository(cc, bucket) ?: throw BucketNotFound(bucket)
        val meta = metaDAO.getMetadataLatest(cc, LadonHistoryKey(bucket, key)) ?: throw DocumentNotFound(bucket, key)

        if (meta.isDeleted()) throw DocumentNotFound(bucket, key)
        val contentMeta = meta.content()
        val resultMeta = Document(bucket, key, meta.key().changeToken.toString(), true, contentMeta.length, contentMeta.hash,
                LocalDateTime.ofInstant(contentMeta.created.toInstant(), ZoneId.systemDefault()), meta.getContentType(), meta.properties().content)
        return ContentDocument(resultMeta, binaryDataDAO.getContentStream(cc, bucket, contentMeta.id, null, null)
                ?: ByteArrayInputStream(ByteArray(0)))
    }

    override fun getDocumentVersion(userId: String, bucket: String, key: String, version: String): ContentDocument {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDocumentVersionMetadata(userId: String, bucket: String, key: String, version: String): Document {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMetadata(userId: String, bucket: String, key: String): Document {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listDocumentVersions(userId: String, bucket: String, key: String): List<Document> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listDocuments(userId: String,
                               bucket: String,
                               prefix: String?,
                               marker: String?,
                               delimiter: String?,
                               limit: Long?): List<Document> {
        val cc = getCC(userId)
        repoDAO.getRepository(cc, bucket) ?: throw BucketNotFound(bucket)
        val result = metaDAO.listAllMetadata(cc, bucket, prefix.orEmpty(), marker, delimiter, (limit
                ?: 1000).toInt(), false)
        val (objectList, prefixes) = result.first
        val truncated = result.second

        return objectList.map {
            val content = it[LadonContentMeta::class]!!
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

    override fun putDocument(userId: String, bucket: String, k: String, content: InputStream): Document {
        val cc = getCC(userId)
       repoDAO.getRepository(cc, bucket) ?: throw BucketNotFound(bucket)

        content.use {
            val info = binaryDataDAO.saveContentStream(cc, bucket, it)
            val hexMd5 = info.md5.drop(2).toUpperCase()


            val key = LadonResourceKey(bucket, k, cc.getCallId().id())
            val meta = LadonMetadata()
            val props = LadonPropertyMeta(hashMapOf("etag" to hexMd5))
            meta.set(props)
            // val name = objectKey.split("/").filterNotNull().last()
            val created = Date()
            meta.set(LadonContentMeta(info.id, hexMd5, info.length.toLong(), created, userId))
            metaDAO.saveMetadata(cc, key, meta)
            return Document(
                    bucket,
                    k,
                    cc.changeToken.toString(),
                    true,
                    info.length.longValueExact(),
                    info.md5,
                    created.toLocalDate(),
                    "",
                    props.content)

        }

    }

    override fun updateDocumentMetadata(userId: String, bucket: String, key: String, version: String, metadata: Map<String, String>): Document {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

