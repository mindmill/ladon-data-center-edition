package de.mc.ladon.server.boot.controller

import de.mc.ladon.rest.api.RestApi
import de.mc.ladon.rest.api.model.Document
import de.mc.ladon.s3server.entities.impl.S3CallContextImpl
import de.mc.ladon.server.s3.LadonS3Storage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
open class RestApiController : RestApi {
    @Autowired
    lateinit var ladonS3Storage: LadonS3Storage

    override fun createNewBucket(bucket: String): ResponseEntity<Void> {
        ladonS3Storage.deleteBucket()
        return ResponseEntity.ok().build()
    }

    override fun deleteDocument(bucket: String?, key: String?): ResponseEntity<Void> {
        return super.deleteDocument(bucket, key)
    }

    override fun deleteDocumentVersion(bucket: String?, key: String?, version: String?): ResponseEntity<Document> {
        return super.deleteDocumentVersion(bucket, key, version)
    }

    override fun deleteEmptyBucket(bucket: String?): ResponseEntity<Void> {
        return super.deleteEmptyBucket(bucket)
    }

    override fun getDocument(bucket: String?, key: String?): ResponseEntity<Void> {
        return super.getDocument(bucket, key)
    }

    override fun getDocumentVersion(bucket: String?, key: String?, version: String?): ResponseEntity<Void> {
        return super.getDocumentVersion(bucket, key, version)
    }

    override fun getDocumentVersionMetadata(bucket: String?, key: String?, version: String?): ResponseEntity<Document> {
        return super.getDocumentVersionMetadata(bucket, key, version)
    }

    override fun getMetadata(bucket: String?, key: String?): ResponseEntity<Document> {
        return super.getMetadata(bucket, key)
    }

    override fun listBuckets(): ResponseEntity<MutableList<String>> {
        return super.listBuckets()
    }

    override fun listDocumentVersions(bucket: String?, key: String?): ResponseEntity<MutableList<Document>> {
        return super.listDocumentVersions(bucket, key)
    }

    override fun listDocuments(bucket: String?, limit: Long?, page: Long?): ResponseEntity<MutableList<Document>> {
        return super.listDocuments(bucket, limit, page)
    }

    override fun putDocument(bucket: String?, key: String?, content: Resource?): ResponseEntity<Document> {
        return super.putDocument(bucket, key, content)
    }

    override fun updateDocumentMetadata(bucket: String?, key: String?, version: String?): ResponseEntity<Document> {
        return super.updateDocumentMetadata(bucket, key, version)
    }
}
