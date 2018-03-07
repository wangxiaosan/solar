package com.wwy.springboot.biz;

import com.wwy.springboot.web.jersey.JerseyConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author wangxiaosan
 * @date 2017/09/29
 *
 * 启动项本来在web下面的server里，网上说的只要配置@ComponentScan就可以扫描到配置的包，测试情况：
 *      controller和service都能扫描到，但是repository的接口一直无法注入，被迫妥协约定，
 *      把启动项移到了代码的所在包的上层。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.wwy.springboot.biz"})
public class StartWorkServer {

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
        return factory;
    }

    @Bean
    public ServletRegistrationBean jerseyServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), "/*");
        // our rest resources will be available in the path /rest/*
        registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, JerseyConfig.class.getName());
        return registration;
    }

    public static void main(String[] args) {
        SpringApplication.run(StartWorkServer.class, args);
    }

}
