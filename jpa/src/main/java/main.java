import Demo.Person;
import org.h2.engine.User;

import javax.persistence.*;
import java.util.Iterator;
import java.util.List;

public class main {
  public static void main(String[] args) {
    //获得Factory
    EntityManagerFactory factory = Persistence.createEntityManagerFactory(
      "JPA"
    );
    //获取Manager
    EntityManager manager = factory.createEntityManager();


    //事务开启
    EntityTransaction transaction = manager.getTransaction();
    transaction.begin();


    //执行sql
    //增
//    注意，对于新对象的插入第一次请使用persist()方法
//    否则对象将无法插入
//    Person ppp = new Person(100,"ppp",12312);
//    manager.persist(ppp);
//
//
//
////   已经插入过的数据请使用merge()方法
//
//    Person person =  new Person(1,"1",200);
//    Person person1 = new Person(2,"2",200);
//    Person person2 = new Person(3,"3",300);
//    Person person3 = new Person(4,"4",400);
//
//    manager.merge(person);
//    manager.merge(person1);
//    manager.merge(person2);
//    manager.merge(person3);
//
//
//
//
//    //删
//    Person person4 =  manager.find(Person.class,2);
//    manager.remove(person4);
//
//
//    //改
//    Person person5 = manager.find(Person.class,3);
//    person5.setUname("20");
//    System.out.println(person3.toString());
//
//
//
//    //查
//    //将游离对象更新到数据库中
//    Person person6 = manager.find(Person.class,4);
//    manager.clear();
//    person6.setUname("2022");
//    manager.merge(person6);
//
//    //普通查询
//    Person person7 = manager.find(Person.class,100);
//    System.out.println(person7.toString());





//
//    String sql = "select * from person where uid = 100";
//    Object result = manager.createNativeQuery(sql).getSingleResult();
//    System.out.println(result);




//    List<Person> persons = (List<Person>) manager.createNativeQuery(
//      ("insert into person (uname,age) values('333',200)"),Person.class).getResultList();

//    System.out.println(persons);

//    System.out.println(query.getSingleResult());


//JPQL
    Query query = manager.createQuery(queryPersonJPQLByParams());
    Object o = query.getResultList();
    System.out.println(o);

    query.executeUpdate();

    String sql = "select p from Person p where uid = :n";

    TypedQuery q2 = manager.createQuery(sql,Person.class);
    List list = q2.getResultList();
    q2.setParameter("n",30);



    //事务提交
    transaction.commit();

    //关闭事务和管理
    manager.close();
    factory.close();




  }

  public static String queryPersonJPQLByParams(){
    String queryQL = "select p from Person p where p.uid=3";

    return queryQL;
  }



}


