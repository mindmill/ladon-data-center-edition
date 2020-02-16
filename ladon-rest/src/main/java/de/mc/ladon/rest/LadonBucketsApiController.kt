package de.mc.ladon.rest

import de.mc.ladon.server.core.api.BucketNotEmpty
import de.mc.ladon.server.core.api.LadonRepository
import de.mc.ladon_gen.rest.api.BucketsApi
import de.mc.ladon_gen.rest.model.Bucket
import de.mc.ladon_gen.rest.model.ResponseSuccess
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.validation.Valid


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Buckets API", tags = ["Buckets"])
open class LadonBucketsApiController @Autowired constructor(
        val ladonRepositoy: LadonRepository
) : BucketsApi {

    override fun createBucket(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket") bucket: String,
            @ApiParam(value = "Optional description in *Markdown*", required = true)
            @Valid @RequestBody body: Bucket?): ResponseEntity<Bucket>? {
        ladonRepositoy.createNewBucket(getUserId(),bucket)
        return ResponseEntity(HttpStatus.OK)
    }


    override fun deleteBucket(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket") bucket: String): ResponseEntity<ResponseSuccess>? {
        try {
            ladonRepositoy.deleteEmptyBucket(getUserId(), bucket)
        } catch (e: BucketNotEmpty) {
            return ResponseEntity.ok(ResponseSuccess().success(false).reason(e.message))
        }
        return ResponseEntity.ok(ResponseSuccess().success(true))
    }


    override fun getBucket(
            @ApiParam(value = "", required = true)
            @PathVariable("bucket") bucket: String): ResponseEntity<Bucket>? {

        return ResponseEntity(HttpStatus.OK)
    }


    override fun listBuckets(
            @ApiParam(value = "")
            @RequestParam(value = "limit", required = false)
            limit: Long?,
            @ApiParam(value = "")
            @RequestParam(value = "page", required = false)
            page: Long?): ResponseEntity<List<Bucket>>? {
        return ResponseEntity.ok(ladonRepositoy.listBuckets(getUserId()).map { Bucket().name(it) })
    }

 private fun getUserId() = SecurityContextHolder.getContext().authentication.name
}
