### JPA是一个能将实体类持久化到数据库中的一个API

>基本注解说明
>>	### @Enity
>>	指出该Java类为实体类，将映射到指定的数据库表
>>	### @Table
>>	当实体类与其映射的数据库表名不同名时用@Table标注说明
>>	### @Table(name="JPA_CUTOMERS")
>>	### @Id
>>	Id用于声明一个实体类的属性映射为数据库的主键列
>>	### @GeneratedValue
>>	用于标注主键的生成策略，通过strategy属性指定
>>	### @Basic
>>	表示一个简单的属性到数据库表的字段的映射
>>	### @Column
>>	当实体的属性与其映射的数据库表的列不同名时用此说明
>>	### @Transient
>>	如果一个属性并非数据库表的字段映射，就务必将其标示为@Transient
>>	否则，ORM框架就将其默认注解为@Basic
>>	### @Temporal
>>	在进行属性映射的时候可以使用@Temporal来调整精度
>


## 注意事项
>	postgresql连接数据库驱动命名方式为“org.postgresql.Driver”
>	@Enity和@Table对应的实例对象在数据库对应的表名都会变成小写
>	且如果类名为长单词会以下划线的形式在数据库表明




