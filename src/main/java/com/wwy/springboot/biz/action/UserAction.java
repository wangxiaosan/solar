package com.wwy.springboot.biz.action;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangxiaosan
 * @date 2017/09/29
 */
@RestController
@RequestMapping(value = "/user")
public class UserAction {

    @RequestMapping(method = RequestMethod.GET,value = "/add")
    public String addUser() {
        String s = "this is spring boot";
        System.out.println(s);
        return s;
    }
}
