package com.wwy.springboot.biz.service.impl;

import com.google.common.collect.Lists;
import com.wwy.springboot.biz.domain.dto.UserDto;
import com.wwy.springboot.biz.domain.entity.User;
import com.wwy.springboot.biz.repository.UserRepository;
import com.wwy.springboot.biz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
@Service
public class DefaultUserService implements UserService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public List<UserDto> findAllUser() {
        List<UserDto> userDtos = Lists.newArrayList();
        List<User> all = userRepository.findAll();
        if(isNull(all) || all.isEmpty()) {
            return userDtos;
        }
        List<UserDto> transform = Lists.transform(all, a -> new UserDto(a.getId(), a.getName(), a.getAddress()));
        return transform;
    }
}
