/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.s3

import com.google.common.io.BaseEncoding
import de.mc.ladon.s3server.common.S3Constants
import de.mc.ladon.s3server.entities.api.*
import de.mc.ladon.s3server.entities.impl.*
import de.mc.ladon.s3server.exceptions.*
import de.mc.ladon.s3server.repository.api.S3Repository
import de.mc.ladon.server.core.persistence.dao.api.BinaryDataDAO
import de.mc.ladon.server.core.persistence.dao.api.MetadataDAO
import de.mc.ladon.server.core.persistence.dao.api.RepositoryDAO
import de.mc.ladon.server.core.persistence.dao.api.UserRoleDAO
import de.mc.ladon.server.core.persistence.entities.api.Repository
import de.mc.ladon.server.core.persistence.entities.api.User
import de.mc.ladon.server.core.persistence.entities.impl.*
import de.mc.ladon.server.core.persistence.entities.impl.Properties
import de.mc.ladon.server.core.util.toUUID
import de.mc.ladon.server.persistence.cassandra.tasks.CleanupOldVersionsTaskRunner
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * on 21.10.16.
 */
@Named
open class LadonS3Storage @Inject constructor(
        val repoDAO: RepositoryDAO,
        val metaDAO: MetadataDAO,
        val userRoleDAO: UserRoleDAO,
        val binaryDataDAO: BinaryDataDAO,
        val versionsCleaner: CleanupOldVersionsTaskRunner,
        val config: LadonS3Config) : S3Repository {


    override fun listAllBuckets(callContext: S3CallContext): List<S3Bucket> {
        return repoDAO.getRepositories(callContext.toLadonCC()).map {
            val cuser = userRoleDAO.getUser(it.createdby!!) ?: throw InternalErrorException("User not found : ${it.createdby}", callContext.requestId)
            S3BucketImpl(it.repoId, it.creationdate, cuser.toS3User())
        }
    }


    override fun createBucket(callContext: S3CallContext, bucketName: String, locationConstraint: String) {
        val lcc = callContext.toLadonCC()
        val repo = repoDAO.getRepository(lcc, bucketName)
        if (repo != null) throw  BucketAlreadyExistsException(bucketName, callContext.requestId)
        repoDAO.addRepository(lcc, bucketName)
    }


    override fun deleteBucket(callContext: S3CallContext, bucketName: String) {
        val lcc = callContext.toLadonCC()
        if (metaDAO.listAllMetadata(lcc, bucketName, "", "", 1, true).first.isEmpty()) {
            repoDAO.deleteRepository(lcc, bucketName)
        } else {
            throw BucketNotEmptyException(bucketName, callContext.requestId)
        }
    }


    override fun createObject(callContext: S3CallContext, bucketName: String, objectKey: String) {
        val lcc = callContext.toLadonCC()
        val repo = repoDAO.getRepository(lcc, bucketName) ?: throw NoSuchBucketException(bucketName, callContext.requestId)
        val contentLength = callContext.header.contentLength
        val md5 = callContext.header.contentMD5
        if (callContext.params.acl()) {
            return
        }
        callContext.content.use {
            val info = binaryDataDAO.saveContentStream(lcc, bucketName, it)
            val hexMd5 = info.md5.drop(2).toUpperCase()
            val storageMd5base64 = BaseEncoding.base64().encode(BaseEncoding.base16().decode(hexMd5))
            if (contentLength != null && contentLength != info.length.toLong() || md5 != null && md5.toUpperCase() != storageMd5base64.toUpperCase()) {
                println("Digest error :MD5 given $md5 , header MD5 $md5")
                if (config.validatecontent ?: false) {
                    binaryDataDAO.deleteContentStream(lcc, bucketName, info.id)
                    throw InvalidDigestException(objectKey, callContext.requestId)
                }
            }
            val key = ResourceKey(bucketName, objectKey, lcc.callId)
            val meta = LadonMetadata()
            meta.set(callContext.header.fullHeader.toProps())
            // val name = objectKey.split("/").filterNotNull().last()
            meta.set(Content(info.id, hexMd5, info.length.toLong(), Date(), lcc.getUser().name))
            metaDAO.saveMetadata(lcc, key, meta)
            val modified = meta.content().created
            val header = S3ResponseHeaderImpl()
            header.setEtag(inQuotes(hexMd5))
            header.setLastModified(modified)
            callContext.setResponseHeader(header)

        }
        if (repo.isUnversioned()) {
            versionsCleaner.cleanupVersions(lcc, bucketName, objectKey)
        }
    }

    override fun copyObject(callContext: S3CallContext, srcBucket: String, srcObjectKey: String, destBucket: String, destObjectKey: String, copyMetadata: Boolean): S3Object {
        val lcc = callContext.toLadonCC()
        repoDAO.getRepository(lcc, srcBucket) ?: throw NoSuchBucketException(srcBucket, callContext.requestId)
        val destRepo = repoDAO.getRepository(lcc, destBucket) ?: throw NoSuchBucketException(srcBucket, callContext.requestId)
        var srcKeyAndVersion = srcObjectKey.split("?versionId=")
        val srcMeta = if (srcKeyAndVersion.size == 2) {
            metaDAO.getMetadata(lcc, ResourceKey(srcBucket, srcKeyAndVersion[0], srcKeyAndVersion[1].toUUID()))
        } else {
            metaDAO.getMetadataLatest(lcc, HistoryKey(srcBucket, srcKeyAndVersion[0]))
        } ?: throw NoSuchKeyException(srcObjectKey, callContext.requestId)
        val content = srcMeta.content()
        val props = srcMeta.properties()

        val newStreamId = binaryDataDAO.copyContentStream(lcc, srcBucket, content.id, destBucket)
        val newContent = content.copy(id = newStreamId, created = Date(), createdBy = lcc.getUser().name)

        val key = ResourceKey(destBucket, destObjectKey, lcc.callId)
        val destMeta = LadonMetadata()
        destMeta.set(callContext.header.fullHeader.toProps())
        if (copyMetadata) {
            destMeta.properties().putAllIfAbsent(props)
        }
        destMeta.set(newContent)
        metaDAO.saveMetadata(lcc, key, destMeta)
        val modified = destMeta.content().created
        if (destRepo.isUnversioned()) {
            versionsCleaner.cleanupVersions(lcc, destBucket, destObjectKey)
        }
        return S3ObjectImpl(
                destObjectKey,
                modified,
                destBucket,
                content.length,
                callContext.user,
                null,
                null,
                null,
                inQuotes(content.hash),
                key.versionSeriesId,
                false,
                true)
    }

    override fun getObject(callContext: S3CallContext, bucketName: String, objectKey: String, head: Boolean) {
        val lcc = callContext.toLadonCC()
        val version = callContext.params.allParams.get(S3Constants.VERSION_ID)
        repoDAO.getRepository(lcc, bucketName) ?: throw NoSuchBucketException(bucketName, callContext.requestId)
        val meta = if (version == null) {
            metaDAO.getMetadataLatest(lcc, HistoryKey(bucketName, objectKey))
        } else {
            metaDAO.getMetadata(lcc, ResourceKey(bucketName, objectKey, UUID.fromString(version)))
        } ?: throw NoSuchKeyException(objectKey, callContext.requestId)

        if (meta.isDeleted()) throw NoSuchKeyException(objectKey, callContext.requestId)
        val content = meta.content()
        val props = meta.properties()
        val header = S3ResponseHeaderImpl(props.toS3Meta())
        header.setContentLength(content.length)
        header.setContentType(props.get(S3Constants.CONTENT_TYPE))
        callContext.setResponseHeader(header)
        if (!head)
            callContext.content = binaryDataDAO.getContentStream(lcc, bucketName, content.id, null, null)
    }


    override fun listBucket(callContext: S3CallContext, bucketName: String): S3ListBucketResult {
        val maxKeys = callContext.params.maxKeys
        val marker = callContext.params.marker ?: ""
        val prefix = callContext.params.prefix ?: ""
        val includeVersions = callContext.params.listVersions()

        val result = metaDAO.listAllMetadata(callContext.toLadonCC(), bucketName, prefix, marker, maxKeys, includeVersions)
        val objectList = result.first
        val truncated = result.second

        // TODO fix latest version hack
        var lastKey: String = ""
        return S3ListBucketResultImpl(objectList.map {
            val latest = lastKey != it.key().versionSeriesId
            lastKey = it.key().versionSeriesId
            val content = it[Content::class]!!
            S3ObjectImpl(it.key().versionSeriesId,
                    it.content().created,
                    bucketName,
                    content.length,
                    callContext.user,
                    it.properties().toS3Meta(),
                    null,
                    it.properties().get(S3Constants.CONTENT_TYPE),
                    content.hash, it.key().changeToken.toString(), it.isDeleted(), latest)
            //TODO
        }, truncated, bucketName, null, null)
    }

    override fun deleteObject(callContext: S3CallContext, bucketName: String, objectKey: String) {
        val lcc = callContext.toLadonCC()
        val repo = repoDAO.getRepository(lcc, bucketName)
        val meta = metaDAO.getMetadataLatest(lcc, HistoryKey(bucketName, objectKey)) ?: throw NoSuchKeyException(objectKey, callContext.requestId)
        metaDAO.deleteMetadata(lcc, meta.key())
        if (repo.isUnversioned()) {
            versionsCleaner.cleanupVersions(lcc, bucketName, objectKey)
        }
    }

    override fun getBucket(callContext: S3CallContext, bucketName: String) {
        val repo = repoDAO.getRepository(callContext.toLadonCC(), bucketName) ?: throw NoSuchBucketException(bucketName, callContext.requestId)
        val cuser = userRoleDAO.getUser(repo.createdby!!) ?: throw InternalErrorException("User not found : ${repo.createdby}", callContext.requestId)
        S3BucketImpl(repo.repoId, repo.creationdate, cuser.toS3User())

    }

    override fun getUser(callContext: S3CallContext, accessKey: String): S3User {
        val keydata = userRoleDAO.getKey(accessKey) ?: throw InvalidAccessKeyIdException("AccessKey not found", callContext.requestId)
        val key = keydata.first
        val user = keydata.second
        return S3UserImpl(user.name, user.name, key.accessKeyId, key.secretKey, user.roles)
    }


    fun S3CallContext.toLadonCC(): LadonS3CallContext {
        return LadonS3CallContext(this)
    }

    private fun Properties.toS3Meta(): S3Metadata {
        val result = HashMap<String, String>(this.content.size)
        for ((k, v) in this.content) {
            if (k.startsWithAny(readFilter)) {
                result.put(k, v)
            }
        }
        return S3MetadataImpl(result)
    }

    private fun Map<String, String>.toProps(): Properties {
        val props = Properties()
        for ((k, v) in this) {
            if (k.startsWithAny(writeFilter))
                props[k] = v
        }
        return props
    }


    private fun User.toS3User(): S3User {
        return S3UserImpl(name, name, null, null, roles)

    }

    private fun inQuotes(etag: String): String {
        return "\"" + etag + "\""
    }

    /*
     * filter headers that should be saved
     */
    val writeFilter = setOf(
            S3Constants.X_AMZ_PREFIX,
            S3Constants.X_AMZ_META_PREFIX,
            S3Constants.EXPIRES.toLowerCase(),
            S3Constants.CONTENT_TYPE.toLowerCase())
    /*
     * filter headers that should be returned as response
     */
    val readFilter = setOf(
            S3Constants.X_AMZ_META_PREFIX,
            S3Constants.EXPIRES.toLowerCase(),
            S3Constants.CONTENT_TYPE.toLowerCase())

    fun String.startsWithAny(prefixes: Set<String>): Boolean {
        return prefixes.any { this.startsWith(it) }
    }


    fun Repository?.isUnversioned(): Boolean {
        return this?.versioned == false
    }

}

