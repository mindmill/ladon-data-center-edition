/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot

import de.mc.ladon.server.boot.config.DatabaseConfigImpl
import de.mc.ladon.server.boot.config.LadonS3ConfigImpl
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet


/**
 * LadonApplication
 * Created by Ralf Ulrich on 27.10.15.
 */
@SpringBootApplication(scanBasePackages = ["de.mc.ladon"])
@EnableConfigurationProperties(DatabaseConfigImpl::class, LadonS3ConfigImpl::class)
open class LadonApplication{


    @Bean
    open fun restApi(parent: ApplicationContext): ServletRegistrationBean {
        return createChildContextServlet(parent, "/services/rest/*", "rest")
    }




    private  fun createChildContextServlet(parent: ApplicationContext, path: String, name: String): ServletRegistrationBean {
        val dispatcherServlet = DispatcherServlet()
        val ctx = AnnotationConfigWebApplicationContext()
        ctx.parent = parent
        ctx.register(PropertyPlaceholderAutoConfiguration::class.java,
                DispatcherServletAutoConfiguration::class.java)
        dispatcherServlet.setApplicationContext(ctx)
        val servletRegistrationBean = ServletRegistrationBean(dispatcherServlet, path)
        servletRegistrationBean.setName(name)
        return servletRegistrationBean
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(LadonApplication::class.java, *args)

}
