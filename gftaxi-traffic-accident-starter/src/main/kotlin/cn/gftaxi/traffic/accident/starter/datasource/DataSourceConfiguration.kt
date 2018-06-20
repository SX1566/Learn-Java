package cn.gftaxi.traffic.accident.starter.datasource

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.util.ClassUtils
import javax.sql.DataSource

/**
 * 多数据库连接配置。
 *
 * See [Configure Two DataSources](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html#howto-two-datasources)
 */
@Configuration
class DataSourceConfiguration {
  /** 交通事故数据库 */
  @Bean
  @Primary
  fun mainDataSource(): DataSource {
    return mainDataSourceProperties().initializeDataSourceBuilder().build()
  }

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  fun mainDataSourceProperties(): DataSourceProperties {
    return DataSourceProperties()
  }

  /** BC 系统数据库 */
  @Bean
  fun bcDataSource(): DataSource {
    val embed = ClassUtils.isPresent("org.hsqldb.jdbcDriver", null)
    return if (embed) { // for development
      EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("classpath:cn/gftaxi/bc/sql/hsql/schema.sql") // from gftaxi-gov-gis-data
        .build()
    } else {            // for production
      bcDataSourceProperties().initializeDataSourceBuilder().build()
    }
  }

  @Bean
  @ConfigurationProperties("bc.datasource")
  fun bcDataSourceProperties(): DataSourceProperties {
    return DataSourceProperties()
  }
}