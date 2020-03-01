package de.mc.ladon.rest

import de.mc.ladon.server.core.api.LadonRepository
import de.mc.ladon_gen.rest.api.DocumentsApi
import de.mc.ladon_gen.rest.model.Document
import de.mc.ladon_gen.rest.model.Metadata
import de.mc.ladon_gen.rest.model.ResponseSuccess
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Documents API", tags = ["Documents"])
open class LadonDocumentsApiController @Autowired constructor(
        val ladonRepositoy: LadonRepository
) : DocumentsApi {


    override fun deleteDocument(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<ResponseSuccess>? {
        return ResponseEntity(HttpStatus.OK)
    }

    override fun getDocument(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Void>? { // do some magic!
        return ResponseEntity(HttpStatus.OK)
    }

    @RequestMapping(value = ["/content/buckets/{bucket}/documents/{*key:.+}"],
            produces = ["application/json"],
            consumes = ["application/octet-stream"],
            method = [RequestMethod.PUT])
    override fun putDocument(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?,
            @ApiParam(value = ""  )
            @Valid
            @RequestBody
            content: Resource): ResponseEntity<Document>? {
       val document = ladonRepositoy.putDocument(getUserId(),bucket,key,content.inputStream)


        return ResponseEntity.ok(Document())
    }

    override fun listDocuments(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", defaultValue = "1000")
            @RequestParam(value = "limit", required = false, defaultValue = "1000")
            limit: Long?,
            @ApiParam(value = "", defaultValue = "1")
            @RequestParam(value = "page", required = false, defaultValue = "1")
            page: Long?,
            @ApiParam(value = "")
            @RequestParam(value = "prefix", required = false)
            prefix: String?, @ApiParam(value = "", allowableValues = "name, created") @RequestParam(value = "orderby", required = false) orderby: String?): ResponseEntity<List<Document?>?>? {

        return ResponseEntity.ok(ladonRepositoy.listDocuments(getUserId(), bucket, prefix, null, null, limit)
                .map {
                    Document()
                            .key(it.key)
                            .contentType(it.contentType)
                            .created(it.created.toString())
                            .owner(getUserId()) // TODO
                            .metadata(it.userMetadata.
                                    let { uM -> Metadata().also { it.putAll(uM) } })
                })
    }


    override fun getDocumentMeta(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String): ResponseEntity<Document>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun putDocumentMeta(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "", required = true)
            @Valid
            @RequestBody
            body: Metadata,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Document>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun listDocumentMetaVersions(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key") key: String): ResponseEntity<List<Document>>? {
        return ResponseEntity(HttpStatus.OK)
    }

    private fun getUserId() = SecurityContextHolder.getContext().authentication.name
}
