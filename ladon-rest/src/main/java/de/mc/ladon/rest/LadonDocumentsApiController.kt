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
import java.io.ByteArrayInputStream
import javax.validation.Valid


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Documents API", tags = ["Documents"])
open class LadonDocumentsApiController @Autowired constructor(
        val ladonRepositoy: LadonRepository
) : DocumentsApi {

    @RequestMapping(value = ["/content/buckets/{bucket}/documents/{key:.+}"],
            produces = ["application/json"],
            method = [RequestMethod.DELETE])
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

        try {
            val document = if (version == null) {
                ladonRepositoy.deleteDocument(getUserId(), bucket, key)
            } else {
                ladonRepositoy.deleteDocumentVersion(getUserId(), bucket, key, version)
            }
        } catch (e: Exception) {
            return ResponseEntity.ok(ResponseSuccess().success(false).reason(e.message))
        }
        return ResponseEntity.ok(ResponseSuccess().success(true))
    }

    @RequestMapping(value = ["/content/buckets/{bucket}/documents/{key:.+}"],
            method = [RequestMethod.GET])
    override fun getDocument(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Void>? {
        TODO()
        return ResponseEntity(HttpStatus.OK)
    }

    @RequestMapping(value = ["/content/buckets/{bucket}/documents/{key:.+}"],
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
            @ApiParam(value = "")
            @Valid
            @RequestBody
            content: Resource): ResponseEntity<Document>? {
        val document = ladonRepositoy.putDocument(getUserId(), bucket, key, content.inputStream)
        return ResponseEntity.ok(document.mapToApiModel())
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
                .map { it.mapToApiModel() })
    }

    @RequestMapping(value = ["/meta/buckets/{bucket}/documents/{key:.+}"],
            produces = ["application/json"],
            method = [RequestMethod.GET])
    override fun getDocumentMeta(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String): ResponseEntity<Document>? {
        val metadata = ladonRepositoy.getMetadata(getUserId(), bucket, key)
        return ResponseEntity.ok(metadata.mapToApiModel())
    }

    @RequestMapping(value = ["/meta/buckets/{bucket}/documents/{key:.+}"],
            produces = ["application/json"],
            method = [RequestMethod.PUT])
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
        val document = if (version != null) {
            ladonRepositoy.updateDocumentMetadata(getUserId(), bucket, key, version, body)
        } else {
            ladonRepositoy.putDocument(getUserId(), bucket, key, emptyStream(), body)
        }
        return ResponseEntity.ok(document.mapToApiModel())
    }


    override fun listDocumentMetaVersions(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key") key: String): ResponseEntity<List<Document>>? {
        val versions = ladonRepositoy.listDocumentVersions(getUserId(), bucket, key).map { it.mapToApiModel() }
        return ResponseEntity.ok(versions)
    }

    private fun de.mc.ladon.server.core.api.Document.mapToApiModel(): Document {
        return Document()
                .key(key)
                .contentType(contentType)
                .size(size)
                .etag(etag)
                .version(version)
                .created(created.toString())
                .owner(getUserId()) // TODO
                .metadata(userMetadata.let { uM -> Metadata().also { it.putAll(uM) } })
    }

    private fun emptyStream() = ByteArrayInputStream(ByteArray(0))
    private fun getUserId() = SecurityContextHolder.getContext().authentication.name
}
