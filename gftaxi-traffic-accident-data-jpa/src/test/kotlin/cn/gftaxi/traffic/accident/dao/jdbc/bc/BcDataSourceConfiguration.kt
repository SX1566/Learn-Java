package cn.gftaxi.traffic.accident.dao.jdbc.bc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * 配置连接 BC 系统数据库的数据源用于单元测试.
 *
 * @author RJ
 */
@Configuration
class BcDataSourceConfiguration @Autowired constructor(
  @Value("\${bc.db.host:localhost}") private val host: String,
  @Value("\${bc.db.port:5432}") private val port: String,
  @Value("\${bc.db.name:bcsystem}") private val name: String,
  @Value("\${bc.db.username:reader}") private val username: String,
  @Value("\${bc.db.password:reader}") private val password: String
) {
  @Bean
  fun bcDataSource(): DataSource {
    return DataSourceBuilder.create()
      .driverClassName("org.postgresql.Driver")
      .url("jdbc:postgresql://$host:$port/$name")
      .username(username)
      .password(password)
      .build()
  }
}