package de.mc.ladon.rest

import de.mc.ladon_gen.rest.api.BucketsApi
import de.mc.ladon_gen.rest.model.Bucket
import io.swagger.annotations.Api
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Buckets API", tags = ["Buckets"])
open class LadonBucketsApiController(

) : BucketsApi {

    override fun metaBucketsGet(limit: Long?, page: Long?): ResponseEntity<MutableList<Bucket>> {
        return ResponseEntity.ok(arrayListOf(Bucket().name("geht")))
    }
}
