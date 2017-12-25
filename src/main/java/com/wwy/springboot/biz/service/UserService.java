package com.wwy.springboot.biz.service;

import com.wwy.springboot.biz.domain.dto.UserDto;
import com.wwy.springboot.biz.domain.entity.User;

import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
public interface UserService {
    List<UserDto> findAllUser();
}
