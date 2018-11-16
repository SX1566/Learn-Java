package com.springbootjpa.springbootjpa.controller;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hello")
public class HelloApi {
  @RequestMapping("/say/{name}")
  String sayHello(@PathVariable String name){
    return "Hello," +  name + "!" ;
  }
}
