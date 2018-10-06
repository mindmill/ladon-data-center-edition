/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.concurrent.TimeUnit;

/**
 * ServletContainerConfig
 * Created by Ralf Ulrich on 17.11.15.
 */
@Configuration
public class ServletContainerConfig {

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return container -> {
            container.addErrorPages(
                    new ErrorPage(HttpStatus.UNAUTHORIZED, "/assets/401.html"),
                    new ErrorPage(HttpStatus.FORBIDDEN, "/assets/403.html"),
                    new ErrorPage(HttpStatus.NOT_FOUND, "/assets/404.html"));
            container.setSessionTimeout(30, TimeUnit.MINUTES);
        };
    }

}
