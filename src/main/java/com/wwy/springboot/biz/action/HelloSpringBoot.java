package com.wwy.springboot.biz.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author wangxiaosan
 * @date 2017/09/29
 */
@Controller
@Path("/hello")
public class HelloSpringBoot {

    private Logger log = LoggerFactory.getLogger(HelloSpringBoot.class);

    @GET
    @Path("/springBoot")
    @Produces(MediaType.APPLICATION_JSON)
    public String helloWord() {
        String s = "this is spring boot";
        log.info("this is info log : "+s);
        log.debug("this is debug log : "+s);
        return s;
    }
}
