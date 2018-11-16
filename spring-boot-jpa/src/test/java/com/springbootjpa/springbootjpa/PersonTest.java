//package com.springbootjpa.springbootjpa;
//
//
//import com.springbootjpa.springbootjpa.dao.PersonDao;
//import com.springbootjpa.springbootjpa.entity.Person;
//import javafx.application.Application;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.transaction.Transactional;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SpringBootJpaApplication.class)
//
//public class PersonTest {
//
//   PersonDao personDao ;
//
//  //增
//  @Test
//  public void testAdd() {
//
//    Person person = new Person();
//    person.setId(2L);
//    person.setName("D");
//    person.setAccount("renwox");
//    person.setPwd("123456");
//    personDao.save(person);
//
//
//    Person person1 = new Person();
//    person1.setId(4L);
//    person1.setName("E");
//    person1.setAccount("linghuc");
//    person1.setPwd("123456");
//    personDao.save(person1);
//
//    Person person2 = new Person();
//    person2.setId(1L);
//    person2.setName("1");
//    person2.setAccount("1");
//    person2.setPwd("1");
//    personDao.save(person2);
//
//    personDao.updateNameByPwd("1", "2332");
//
//    personDao.deleteById(4L);
//
//    Person person3 = personDao.findByNameAndPwdAndAndAccountAndId("1", "1", "1", 1L);
//    System.out.println(person3);
//
//
//
//
//  }
//
//  //  改
//
//  public void testUpdate() {
//    personDao.updateNameByPwd("1", "2332");
//
//
//  }
//
//
//  public void testDelete() {
//    personDao.deleteById(4L);
//
//  }
//
//  //  获取数据
//
//  public void testFind() {
//    Person person = personDao.findByNameAndPwdAndAndAccountAndId("1", "1", "1", 1L);
//    System.out.println(person);
//
//
//  }
//
//
//}
