package de.mc.ladon.rest;


import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class RestConfig {

    //@Bean
    ServletRegistrationBean restApi(ApplicationContext ctx) {
        return createChildContextServlet(ctx);
    }

    private ServletRegistrationBean createChildContextServlet(ApplicationContext parent) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        // ctx.setParent(parent);
        ctx.scan("de.mc.ladon3");

        dispatcherServlet.setApplicationContext(ctx);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/services/rest/*");
        servletRegistrationBean.setName("rest");
        servletRegistrationBean.setLoadOnStartup(0);
        return servletRegistrationBean;
    }
}
