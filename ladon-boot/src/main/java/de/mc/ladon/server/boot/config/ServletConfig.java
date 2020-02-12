/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschr�nkt)
 */

package de.mc.ladon.server.boot.config;

import de.mc.ladon.s3server.logging.PerformanceLoggingFilter;
import de.mc.ladon.s3server.repository.api.S3Repository;
import de.mc.ladon.s3server.servlet.S3Servlet;
import de.mc.ladon.server.s3.LadonS3Config;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HttpServletBean;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ServletConfig
 * Created by Ralf Ulrich on 17.11.15.
 */
@Configuration
public class ServletConfig {


    @Autowired
    private S3Repository s3r;

    @Bean
    public ServletRegistrationBean redirectServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new HttpServletBean() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.sendRedirect("/ladon/overview");
            }
        });
        registration.setName("redirectServlet");
        registration.addUrlMappings("/");
        registration.setLoadOnStartup(1);
        return registration;
    }


    @Bean
    public ServletRegistrationBean s3ServletRegistrationBean(LadonS3Config config) {
        S3Servlet s3Servlet = new S3Servlet(config.getServletthreads());
        s3Servlet.setSecurityEnabled(config.getDisableSecurity() == null
                || !config.getDisableSecurity());
        s3Servlet.setRequestTimeout(config.getRequesttimeout());
        s3Servlet.setRepository(s3r);
        ServletRegistrationBean registration = new ServletRegistrationBean(
                s3Servlet);
        registration.setName("s3servlet");
        registration.addUrlMappings("/services/s3/*");
        registration.setLoadOnStartup(1);
        return registration;
    }


    @Bean
    CmisRepositoryContextListener cmisListener() {
        return new CmisRepositoryContextListener();
    }

    @Bean
    public ServletRegistrationBean cmisServletRegistrationBean() {
        CmisBrowserBindingServlet cmisServlet = new CmisBrowserBindingServlet();
        ServletRegistrationBean registration = new ServletRegistrationBean(cmisServlet);
        Map<String, String> initParams = new HashMap<>();
//        initParams.put("template", "/WEB-INF/cmis-endpoints.json");
        initParams.put("callContextHandler", "org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        registration.setInitParameters(initParams);
        registration.setName("cmisServlet");
        registration.addUrlMappings("/services/cmis/*");
        registration.setLoadOnStartup(2);
        return registration;
    }


    @Bean
    @ConditionalOnProperty(value = "ladon.s3.loggingenabled", havingValue = "true")
    FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterBean = new FilterRegistrationBean();
        filterBean.setFilter(new PerformanceLoggingFilter());
        filterBean.addServletNames("s3servlet");
        filterBean.setAsyncSupported(true);
        return filterBean;
    }

    @Bean
    FilterRegistrationBean corsFilterRegistrationBean() {

        FilterRegistrationBean filterBean = new FilterRegistrationBean();
        filterBean.setFilter(new CorsFilter());
        filterBean.addServletNames("s3servlet");
        filterBean.setAsyncSupported(true);
        return filterBean;
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration(MultipartConfigElement multipartConfigElement) {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                dispatcherServlet(), "/ladon/*", "/login");
        registration.setMultipartConfig(multipartConfigElement);
        registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
        return registration;
    }

}
