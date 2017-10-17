package com.wwy.springboot.web.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author wangxiaosan
 * @date 2017/09/29
 */
@SpringBootApplication
@ComponentScan("com.wwy.springboot.biz")
public class StartWorkServer {

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
        return factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(StartWorkServer.class, args);
    }

}
