package de.mc.ladon.server.boot.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomContainer implements
        WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addErrorPages(
                new ErrorPage(HttpStatus.UNAUTHORIZED, "/assets/401.html"),
                new ErrorPage(HttpStatus.FORBIDDEN, "/assets/403.html"),
                new ErrorPage(HttpStatus.NOT_FOUND, "/assets/404.html"));
    }
}
