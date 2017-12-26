package com.wwy.springboot.sample.data.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author wangxiaosan
 * @date 2017/12/15
 */
@Entity
@Table(name = "user")
public class User implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "id")
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
