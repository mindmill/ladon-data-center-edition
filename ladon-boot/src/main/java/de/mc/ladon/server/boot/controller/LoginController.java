/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * LoginController
 * Created by Ralf Ulrich on 01.12.15.
 */
@Controller
public class LoginController {


    @RequestMapping("login")
    public String welcome(Map<String, Object> model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.put("_csrf", csrfToken);
        }
        return "templates/login";
    }


}
