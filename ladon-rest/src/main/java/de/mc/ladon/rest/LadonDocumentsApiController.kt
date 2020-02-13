package de.mc.ladon.rest

import de.mc.ladon_gen.rest.api.DocumentsApi
import io.swagger.annotations.Api
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Documents API", tags = ["Documents"])
open class LadonDocumentsApiController : DocumentsApi {


}
