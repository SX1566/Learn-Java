package com.springbootjpa.springbootjpa.controller;

import com.springbootjpa.springbootjpa.entity.Person;
import com.springbootjpa.springbootjpa.Service.PersonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PersonController {
  @Autowired
  private PersonServiceImpl personService;

  @PostMapping("/addPerson")
  protected CommonResult addPerson(Person person) {
    CommonResult result = new CommonResult();
    try {
      personService.addPerson(person);
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      result.setState(500);
      result.setMsg("失败");
      return result;
    }
  }

  @PutMapping("/updatePerson")
  public CommonResult updatePersonById(Person person) {
    CommonResult result = new CommonResult();
    try {
      personService.updatePerson(person);
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      result.setState(500);
      result.setMsg("失败");
      return result;
    }
  }

  @DeleteMapping("/deletePerson/{id}")
  public CommonResult deletePersonById(@PathVariable(name = "id", required = true) Long id) {
    CommonResult result = new CommonResult();
    try {
      personService.deletePersonById(id);
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      result.setState(500);
      result.setMsg("失败");
      return result;
    }
  }

//  查询所有
  @GetMapping("/findAll")
  public CommonResult findAll(){
    CommonResult result = new CommonResult();
    try{
      List<Person> list = personService.findAll();
      //将结果封装到CommonResult中
      result.setData(list);
      return  result;
    }catch (Exception e){
      e.printStackTrace();
      result.setState(500);
      result.setMsg("失败");
      return result;
    }
  }

  //根据id查询一条数据
  @GetMapping("/findStudentById/{id}")
  public CommonResult findStudentById(@PathVariable(name = "id") Long id){
    CommonResult result = new CommonResult();
    try {
      Person person = personService.findPersonById(id);
      //将查询结果封装
      result.setData(person);
      return result;

    }catch (Exception e){
      e.printStackTrace();
      result.setState(500);
      result.setMsg("失败");
      return result;
    }

  }

  //根据学生姓名查询
  @GetMapping("/findPersonByName")
  public CommonResult findPersonByName(String name){
    CommonResult result = new CommonResult();
    try {
      List<Person> personList = personService.findPersonByName(name);
      result.setData(personList);
      return result;

    }catch (Exception e ){
      e.printStackTrace();
      result.setState(500);
      result.setMsg("失败");
      return result;
    }
  }


}











