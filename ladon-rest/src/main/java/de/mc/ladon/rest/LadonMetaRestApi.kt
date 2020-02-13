package de.mc.ladon.rest

import de.mc.ladon_gen.rest.api.MetaApi
import io.swagger.annotations.Api
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/api/rest/v1")
@Api(value = "meta", description = "Ladon Metadata API", tags = ["Metadata"])
open class LadonMetaRestApi : MetaApi {


}
