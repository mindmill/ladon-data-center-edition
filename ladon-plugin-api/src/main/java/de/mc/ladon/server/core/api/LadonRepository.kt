package de.mc.ladon.server.core.api

import java.io.InputStream
import java.time.LocalDateTime

interface LadonRepository {

    @Throws(UserNotFound::class,
            BucketAlreadyExists::class)
    fun createNewBucket(userId: String,
                        bucket: String)

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun deleteDocument(userId: String,
                       bucket: String,
                       key: String): Document

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun deleteDocumentVersion(userId: String,
                              bucket: String,
                              key: String,
                              version: String): Document

    @Throws(UserNotFound::class,
            BucketNotEmpty::class)
    fun deleteEmptyBucket(userId: String,
                          bucket: String)

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun getDocument(userId: String,
                    bucket: String,
                    key: String): ContentDocument

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun getDocumentVersion(userId: String,
                           bucket: String,
                           key: String,
                           version: String): ContentDocument

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun getDocumentVersionMetadata(userId: String,
                                   bucket: String,
                                   key: String,
                                   version: String): Document

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun getMetadata(userId: String,
                    bucket: String,
                    key: String): Document

    @Throws(UserNotFound::class)
    fun listBuckets(userId: String): List<String>

    @Throws(UserNotFound::class,
            DocumentIllegalAccess::class,
            DocumentNotFound::class)
    fun listDocumentVersions(userId: String,
                             bucket: String,
                             key: String): List<Document>

    @Throws(UserNotFound::class,
            BucketIllegalAccess::class,
            BucketNotFound::class)
    fun listDocuments(userId: String,
                      bucket: String,
                      prefix: String?,
                      marker: String?,
                      delimiter: String?,
                      limit: Long?): List<Document>

    @Throws(UserNotFound::class,
            BucketIllegalAccess::class,
            BucketNotFound::class)
    fun putDocument(userId: String,
                    bucket: String,
                    key: String,
                    content: InputStream): Document

    @Throws(UserNotFound::class,
            BucketIllegalAccess::class,
            BucketNotFound::class,
            DocumentNotFound::class,
            DocumentIllegalAccess::class)
    fun updateDocumentMetadata(userId: String,
                               bucket: String,
                               key: String,
                               version: String,
                               metadata: Map<String,String>): Document
}

data class Document(
        val bucket: String,
        val key: String,
        val version: String,
        val latest: Boolean,
        val size: Long,
        val etag: String,
        val created: LocalDateTime,
        val contentType: String,
        val userMetadata: Map<String, String>)

data class ContentDocument(val meta: Document,
                           val content: InputStream)

sealed class LadonRepositoryException(override val message: String, val status: Int) : Exception(message)
data class UserNotFound(val userId: String) : LadonRepositoryException("The user $userId could not be found", 403)
data class BucketAlreadyExists(val bucket: String) : LadonRepositoryException("The Bucket $bucket already exists", 409)
data class BucketNotFound(val bucket: String) : LadonRepositoryException("The Bucket $bucket could not be found", 404)
data class BucketNotEmpty(val bucket: String) : LadonRepositoryException("The Bucket $bucket is not empty", 409)
data class DocumentNotFound(val bucket: String, val key: String) : LadonRepositoryException("The Document $key is not present in bucket  $bucket", 404)
data class BucketIllegalAccess(val bucket: String) : LadonRepositoryException("No permission to access bucket $bucket", 403)
data class DocumentIllegalAccess(val bucket: String, val key: String) : LadonRepositoryException("No permission to access the Document $key in bucket  $bucket", 403)
