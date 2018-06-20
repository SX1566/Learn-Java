#  GFTaxi traffic-accident Server Starter

交通事故微服务。

## Requirement

- Maven 3.5.2+
- Kotlin 1.2.31+
- Java 8+
- Spring Framework 5+
- Spring Boot 2+
- Reactor 3+

## Maven Profiles

Environment | Profile           | Persistence  | Remark
------------|-------------------|--------------|--------
Development | **dev-jpa-hsql**  | [HyperSQL]   | JPA
Production  | prod-jpa-postgres | [PostgreSQL] | JPA

默认的 profile 是 `dev-jpa-hsql`，命令行通过 -P 参数指定：

```bash
mvn spring-boot:run -P {profile-name}
```

默认的 Web 服务端口为 9102， 命令行通过 `-D port=9102` 参数指定。

## Maven Properties

Property Name | Default Value | Remark
--------------|---------------|--------
port          | 9102          | Web server port
db.host       | localhost     | Database host
db.name       | accident      | Database name
db.username   | password      | Database connect username
db.password   | password      | Database connect password
db.init-mode  | never         | Init database by `spring.datasource.schema/data` config. `never` or `always`

命令行通过 `-D {property-name}={property-value}` 来覆盖默认值，如：

```bash
mvn spring-boot:run -D port=9102
```

## Build Production

```bash
mvn clean package -P prod-{xxx}
```

## Run Production

```bash
java -jar {package-name}.jar

# or
nohup java -jar {package-name}.jar > /dev/null &
```

## Run Integration Test

连接到真正服务器的测试：

1. 启动 Web Server：`mvn spring-boot:run`
2. 运行集成测试类 [IntegrationTest.kt]


[Embedded MongoDB]: https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo#embedded-mongodb
[MongoDB]: https://www.mongodb.com
[HyperSQL]: http://hsqldb.org
[PostgreSQL]: https://www.postgresql.org
[IntegrationTest.kt]: https://gitee.com/gftaxi/gftaxi-traffic-accident/blob/master/gftaxi-traffic-accident-starter/src/test/kotlin/cn/gftaxi/traffic/accident/starter/IntegrationTest.kt