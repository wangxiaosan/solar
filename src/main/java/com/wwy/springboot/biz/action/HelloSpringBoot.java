package com.wwy.springboot.biz.action;

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

    @GET
    @Path("/springBoot")
    @Produces(MediaType.APPLICATION_JSON)
    public String helloWord() {
        String s = "this is spring boot";
        System.out.println(s);
        return s;
    }
}
