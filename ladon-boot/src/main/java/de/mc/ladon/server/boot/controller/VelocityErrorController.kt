package de.mc.ladon.server.boot.controller

import de.mc.ladon.server.boot.controller.pages.ErrorReport
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

/**
 * VelocityErrorController
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class VelocityErrorController : ErrorController {

    private val LOG = LoggerFactory.getLogger(javaClass)

    override fun getErrorPath(): String? {
        return "/templates/error"
    }


    @RequestMapping("/error")
    fun error(model: MutableMap<String, Any>, request: HttpServletRequest): String {
        val ex = request.getAttribute("javax.servlet.error.exception") as Throwable?
        model.put("errorReport", ErrorReport(ex, request))
        return "error"
    }


}
