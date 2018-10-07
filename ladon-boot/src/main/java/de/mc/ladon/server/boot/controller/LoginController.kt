/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot.controller

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

import javax.servlet.http.HttpServletRequest

/**
 * LoginController
 * Created by Ralf Ulrich on 01.12.15.
 */
@Controller
class LoginController {


    @RequestMapping("login")
    fun welcome(model: MutableMap<String, Any>, request: HttpServletRequest): String {
        val csrfToken = request.getAttribute(CsrfToken::class.java.name) as CsrfToken?
        if (csrfToken != null) {
            model["_csrf"] = csrfToken
        }
        if(request.getParameter("error") != null) model["flash"] = "danger" to "Wrong username or password"
        return "templates/login"
    }


}
