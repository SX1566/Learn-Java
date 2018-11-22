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

Environment | Profile              | Persistence  | Remark
------------|----------------------|--------------|--------
Development | dev-jpa-hsql         | [HyperSQL]   | JPA
Development | **dev-jpa-postgres** | [PostgreSQL] | JPA
Production  | prod-jpa-postgres    | [PostgreSQL] | JPA

默认的 profile 是 `dev-jpa-postgres`，命令行通过 -P 参数指定：

```bash
mvn spring-boot:run -P {profile-name}
```

默认的 Web 服务端口为 9102， 命令行通过 `-D port=9102` 参数指定。

## Maven Properties

Profile `dev-jpa-postgres` :

Property Name   | `dev-jpa-postgres` | `prod-jpa-postgres` | Remark
----------------|--------------------|---------------------|--------
port            | 9102               | 9102                | Web server port
db.host         | localhost          | localhost           | Main database host
db.name         | test_accident      | accident            | Main database name
db.username     | test               | accident            | Main database connect username
db.password     | password           | password            | Main database connect password
db.init-mode    | never              | never               | `never` or `always`. If `always`, init main database by `spring.datasource.schema/data` config.
bc.db.host      | localhost          | localhost           | BC database host
bc.db.name      | test_bcsystem      | bcsystem            | BC database name
bc.db.username  | test               | bcsystem            | BC database connect username
bc.db.password  | password           | password            | BC database connect password
bc.db.init-mode | never              | never               | `never` or `always`

命令行通过 `-D {property-name}={property-value}` 来覆盖默认值，如：

```bash
mvn spring-boot:run -D port=9102
```

## 数据库初始化 （Profile `dev-jpa-postgres` 和 `prod-jpa-postgres`）

本微服务的数据库创建后，按如下顺序执行数据库初始化脚本：

1. [simter-kv/.../postgres/schema-create.sql](https://github.com/simter/simter-kv/blob/0.2.0/simter-kv-data/src/main/resources/tech/simter/kv/sql/postgres/schema-create.sql)
2. [simter-kv/.../postgres/data.sql](https://github.com/simter/simter-kv/blob/0.2.0/simter-kv-data/src/main/resources/tech/simter/kv/sql/postgres/data.sql)
3. [simter-category/.../postgres/schema-create.sql](https://github.com/simter/simter-category/blob/0.1.0/simter-category-data/src/main/resources/tech/simter/category/sql/postgres/schema-create.sql)
4. [simter-category/.../postgres/data.sql](https://github.com/simter/simter-category/blob/0.1.0/simter-category-data/src/main/resources/tech/simter/category/sql/postgres/data.sql)
5. [gftaxi-traffic-accident/.../accident/sql/postgres/schema.sql](https://gitee.com/gftaxi/gftaxi-traffic-accident/blob/master/gftaxi-traffic-accident-data/src/main/resources/cn/gftaxi/traffic/accident/sql/postgres/schema.sql)
6. [gftaxi-traffic-accident/.../accident/sql/postgres/data.sql](https://gitee.com/gftaxi/gftaxi-traffic-accident/blob/master/gftaxi-traffic-accident-data/src/main/resources/cn/gftaxi/traffic/accident/sql/postgres/data.sql)
7. 更新相应模块的版本号信息：
    ```
    update st_kv set value = '0.2.0' where key = 'module-version-simter-kv';
    update st_kv set value = '0.1.0' where key = 'module-version-simter-category';
    update st_kv set value = '{project.version}' where key = 'module-version-gftaxi-traffic-accident';
    ```

在 BC 系统数据库上执行如下脚本：（初始化 BC 系统的事故权限设置）

1. [gftaxi-traffic-accident-ui-web/.../accident/sql/postgres/data4bc.sql](https://gitee.com/gftaxi/gftaxi-traffic-accident/blob/master/gftaxi-traffic-accident-ui-web/src/main/resources/cn/gftaxi/traffic/accident/sql/postgres/data4bc.sql)

## Build Production

```bash
mvn clean package -P prod-jpa-postgres
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