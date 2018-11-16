package com.springbootjpa.springbootjpa.dao;

import com.springbootjpa.springbootjpa.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PersonDao extends JpaRepository<Person,Long> {

//   查询
      List<Person> findByName(String name);

//    Person findByNameAndPwdAndAndAccountAndId(String name,String pwd,String account,Long id);
//
//
////    查询全部
//    List<Person> findAll();
//
//    //分页查询
//
//  Page<Person> findAll(PageRequest pageRequest);
//    Page<Person> findAll(Pageable pageable);
//
//    @Modifying
//    @Transactional
//    @Query("update Person as c set c.name = ?1 where c.pwd=?2")
//  int updateNameByPwd(String name,String pwd);


}
