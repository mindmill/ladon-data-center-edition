package de.mc.ladon.rest

import de.mc.ladon_gen.rest.api.DocumentsApi
import de.mc.ladon_gen.rest.model.Document
import de.mc.ladon_gen.rest.model.Metadata
import de.mc.ladon_gen.rest.model.ResponseSuccess
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Documents API", tags = ["Documents"])
open class LadonDocumentsApiController : DocumentsApi {


    override fun contentBucketsBucketDocumentsKeyDelete(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<ResponseSuccess?>? {
        return ResponseEntity(HttpStatus.OK)
    }

    override fun contentBucketsBucketDocumentsKeyGet(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Void?>? { // do some magic!
        return ResponseEntity(HttpStatus.OK)
    }


    override fun contentBucketsBucketDocumentsKeyPut(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String,
            @ApiParam(value = "", required = true)
            @PathVariable("key")
            key: String,
            @ApiParam(value = "")
            @RequestParam(value = "version", required = false)
            version: String?): ResponseEntity<Document?>? {
        return ResponseEntity(HttpStatus.OK)
    }

    override fun metaBucketsBucketDocumentsGet(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket")
            bucket: String?,
            @ApiParam(value = "", defaultValue = "1000")
            @RequestParam(value = "limit", required = false, defaultValue = "1000")
            limit: Long?,
            @ApiParam(value = "", defaultValue = "1")
            @RequestParam(value = "page", required = false, defaultValue = "1")
            page: Long?,
            @ApiParam(value = "")
            @RequestParam(value = "prefix", required = false)
            prefix: String?, @ApiParam(value = "", allowableValues = "name, created") @RequestParam(value = "orderby", required = false) orderby: String?): ResponseEntity<List<Document?>?>? {
        return ResponseEntity.ok(arrayListOf(Document().key("test.txt")
                .metadata(Metadata().also { it["gelesen"] = "true" })))
    }


    override fun metaBucketsBucketDocumentsKeyDelete(@ApiParam(value = "", required = true) @PathVariable("bucket") bucket: String?, @ApiParam(value = "", required = true) @PathVariable("key") key: String?, @ApiParam(value = "") @RequestParam(value = "version", required = false) version: String?): ResponseEntity<ResponseSuccess?>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun metaBucketsBucketDocumentsKeyGet(@ApiParam(value = "", required = true) @PathVariable("bucket") bucket: String?, @ApiParam(value = "", required = true) @PathVariable("key") key: String?): ResponseEntity<Document?>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun metaBucketsBucketDocumentsKeyPut(@ApiParam(value = "", required = true) @PathVariable("bucket") bucket: String?, @ApiParam(value = "", required = true) @PathVariable("key") key: String?, @ApiParam(value = "") @RequestParam(value = "version", required = false) version: String?): ResponseEntity<Document?>? {
        return ResponseEntity(HttpStatus.OK)
    }


    override fun metaBucketsBucketDocumentsKeyVersionsGet(@ApiParam(value = "", required = true) @PathVariable("bucket") bucket: String?, @ApiParam(value = "", required = true) @PathVariable("key") key: String?): ResponseEntity<List<Document?>?>? {
        return ResponseEntity(HttpStatus.OK)
    }


}
