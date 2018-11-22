package cn.gftaxi.traffic.accident.bc.dao.jdbc

import org.slf4j.LoggerFactory
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
  @Value("\${bc.db.name:test_bcsystem}") private val name: String,
  @Value("\${bc.db.username:test}") private val username: String,
  @Value("\${bc.db.password:password}") private val password: String
) {
  private val logger = LoggerFactory.getLogger(BcDataSourceConfiguration::class.java)

  init {
    logger.info("host=$host")
    logger.info("port=$port")
    logger.info("name=$name")
    logger.info("username=$username")
    logger.info("password=$password")
  }

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