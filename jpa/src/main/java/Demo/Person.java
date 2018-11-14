package Demo;

import javax.persistence.*;

//数据库对应名不一样使用Table
//@Table(name = "")
@Entity
public class Person {
  //标注id
  @Id
  private int uid;
//  用Column属性声明一些行
//  @Column(nullable = true)
  @Basic
  private String uname;
  @Basic
  private int age;

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getUname() {
    return uname;
  }

  public void setUname(String uname) {
    this.uname = uname;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public String toString() {
    return "Users [uid=" + uid + ", uname=" + uname + ", age=" + age + "]";
  }

  public Person(int uid, String uname, int age) {
    super();
    this.uid = uid;
    this.uname = uname;
    this.age = age;
  }

  public Person() {
    super();
    // TODO Auto-generated constructor stub
  }


}

