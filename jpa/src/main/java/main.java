import Demo.Person;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class main {
  public static void main(String[] args) {
    //获得Factory
    EntityManagerFactory factory = Persistence.createEntityManagerFactory(
      "JPA"
    );
    //获取Manager
    EntityManager manager = factory.createEntityManager();

    //事务
    EntityTransaction transaction = manager.getTransaction();
    transaction.begin();


    //执行sql
    Person person = new Person(100, "100", 200);
    manager.persist(person);

    //事务提交
    transaction.commit();


    //执行搜索sql
    Person person2 = manager.find(Person.class,"100");
    System.out.println(person2);

    manager.close();
    factory.close();











  }
}


