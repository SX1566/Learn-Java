package cn.gftaxi.traffic.accident.dao.jpa

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

private const val MODULE = "cn.gftaxi.traffic.accident"

/**
 * All configuration for this module.
 *
 * @author JF
 */
@Configuration("$MODULE.dao.jpa.ModuleConfiguration")
@ComponentScan("$MODULE.dao.jpa", "$MODULE.dao.jdbc")
@EnableJpaRepositories("$MODULE.dao.jpa")
@EntityScan("$MODULE.po", "$MODULE.dto")
class ModuleConfiguration