package com.wwy.springboot.sample.data.repository;

import com.wwy.springboot.sample.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long>, QueryDslPredicateExecutor<User> {

}
