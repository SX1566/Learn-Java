package com.springbootjpa.springbootjpa.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 用户实体类

 */
@Entity
@Table(name = "Person3")
public class Person implements Serializable {
    @Id
    private Long id;
    @Basic
    private String name;
    @Basic

    private String account;
    @Basic
    private String pwd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

  @Override
  public String toString() {
    return "Person{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", account='" + account + '\'' +
      ", pwd='" + pwd + '\'' +
      '}';
  }
}
