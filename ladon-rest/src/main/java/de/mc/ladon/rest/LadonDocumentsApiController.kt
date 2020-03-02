package de.mc.ladon.rest

import de.mc.ladon.server.core.api.LadonRepository
import de.mc.ladon_gen.rest.api.DocumentsApi
import de.mc.ladon_gen.rest.model.Document
import de.mc.ladon_gen.rest.model.ResponseSuccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@Controller
@RequestMapping("/api/rest/v1")
open class LadonDocumentsApiController @Autowired constructor(
        val ladonRepositoy: LadonRepository
) : DocumentsApi {


    override fun deleteDocument(
            @PathVariable("bucket")
            bucket: String,
            @PathVariable("key")
            key: String,
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<ResponseSuccess>? {
        return ResponseEntity(HttpStatus.OK)
    }

    override fun getDocument(
            @PathVariable("bucket")
            bucket: String,
            @PathVariable("key")
            key: String,
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Void>? { // do some magic!
        return ResponseEntity(HttpStatus.OK)
    }

    @RequestMapping(value = ["/content/buckets/{bucket}/documents/{*key:.+}"],
            produces = ["application/json"],
            consumes = ["application/octet-stream"],
            method = [RequestMethod.PUT])
    override fun putDocument(
            @PathVariable("bucket")
            bucket: String,
            @PathVariable("key")
            key: String,
            @RequestParam(value = "version", required = false)
            version: String?,
            @Valid
            @RequestBody
            content: String): ResponseEntity<Document>? {
       val document = ladonRepositoy.putDocument(getUserId(),bucket,key,content.toByteArray().inputStream())


        return ResponseEntity.ok(Document())
    }

    override fun listDocuments(
            @PathVariable("bucket")
            bucket: String,
            @RequestParam(value = "limit", required = false, defaultValue = "1000")
            limit: Long?,
            @RequestParam(value = "page", required = false, defaultValue = "1")
            page: Long?,
            @RequestParam(value = "prefix", required = false)
            prefix: String?,
            @RequestParam(value = "orderby", required = false) orderby: String?): ResponseEntity<List<Document?>?>? {

        return ResponseEntity.ok(ladonRepositoy.listDocuments(getUserId(), bucket, prefix, null, null, limit)
                .map {
                    Document()
                            .key(it.key)
                            .contentType(it.contentType)
                            .created(it.created.toString())
                            .owner(getUserId()) // TODO
                            .metadata(it.userMetadata)
                })
    }


    override fun getDocumentMeta(
            @PathVariable("bucket")
            bucket: String,
            @PathVariable("key")
            key: String): ResponseEntity<Document>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun putDocumentMeta(
            @PathVariable("bucket")
            bucket: String,
            @PathVariable("key")
            key: String,
            @Valid
            @RequestBody
            body: Map<String,String>,
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Document>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun listDocumentMetaVersions(
            @PathVariable("bucket")
            bucket: String,
            @PathVariable("key") key: String): ResponseEntity<List<Document>>? {
        return ResponseEntity(HttpStatus.OK)
    }

    private fun getUserId() = SecurityContextHolder.getContext().authentication.name
}
