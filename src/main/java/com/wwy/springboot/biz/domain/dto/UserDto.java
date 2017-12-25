package com.wwy.springboot.biz.domain.dto;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
public class UserDto {

    public UserDto(long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    long id;
    String name;
    String address;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
