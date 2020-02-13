package de.mc.ladon.rest

import de.mc.ladon_gen.rest.api.ContentApi
import io.swagger.annotations.Api
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Content API", tags = ["Content"])
open class LadonContentRestApi : ContentApi {


}
