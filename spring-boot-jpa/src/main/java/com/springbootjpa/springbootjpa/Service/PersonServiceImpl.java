package com.springbootjpa.springbootjpa.Service;

import com.springbootjpa.springbootjpa.dao.PersonDao;
import com.springbootjpa.springbootjpa.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public  class PersonServiceImpl {

@Autowired
  PersonDao personDao;

  //插入一个学生
  public void addPerson(Person person){
    personDao.save(person);
  }

  //修改
  public void updatePerson(Person person){
    personDao.save(person);
  }

  //删除
  public void deletePersonById(Long id){
    personDao.deleteById(id);
  }

//  查询所有
  public List<Person> findAll(){
    return personDao.findAll();
  }

  //根据id查询一条数据
  public Person findPersonById(Long id){
    return personDao.findById(id).get();
  }

  //根据学生姓名查询多条数据
  public List<Person> findPersonByName(String name){
    return personDao.findByName(name);
  }

}
