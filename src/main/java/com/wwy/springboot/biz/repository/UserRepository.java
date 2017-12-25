package com.wwy.springboot.biz.repository;

import com.wwy.springboot.biz.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    List<User> findAll();
}
