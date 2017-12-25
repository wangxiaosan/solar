package com.wwy.springboot.biz.domain.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
@Entity
@NamedQuery(name = "User.findByName", query = "select id,name,address from User u where u.name=?1")
public class User implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    long id;
    @Column(name = "name")
    String name;
    @Column(name = "address")
    String address;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
}
