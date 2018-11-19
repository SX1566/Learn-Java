#### Spring-Data-Jpa

## 此处为个人笔记，必要时请参考官方文档[*Spring Data Jpa*](https://spring.io/projects/spring-data-jpa)

Spring-Data-Jpa是spring Data大家族的一员，spring data是为了方便我们更好的操作数据库，其中包含了例如Spring Data Jdbc、Spring Data Redis、Spring Data MongoDB这些框架。
Spring-Data-Jpa是封装了基于Jpa框架的一些方法，在使用过程中可以非常方便的使用一些CRUD的操作
废话不多说，介绍spring-data-jpa的好处在哪

首先还是和Jpa一样，新建一个实体类，用@Entity进行注解，用@Table或@Id声明你要操作的数据库表名

``` java
@Entity
@Table(name = "Person3")
public class Person implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @Basic
    private String name;
    @Basic
    private String account;
    @Basic
    private String pwd; 
 /*
 省略get/set和toString方法
 */ 
}
```
接着写一个接口，里面继承一个由Spring-Data-Jpa提供的一个接口
``` java
public interface PersonDao extends JpaRepository<Person,Long> {

}
```
与Jpa不同，spring-data-jpa为我们提供的这个JpaRepository接口里面包含了简单的CRUD方法







