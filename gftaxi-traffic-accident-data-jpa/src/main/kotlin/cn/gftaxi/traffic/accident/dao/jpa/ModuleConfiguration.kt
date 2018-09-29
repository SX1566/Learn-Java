package cn.gftaxi.traffic.accident.dao.jpa

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

private const val MODULE = "cn.gftaxi.traffic.accident"

/**
 * All configuration for this module.
 *
 * @author RJ
 */
@Configuration("$MODULE.dao.jpa.ModuleConfiguration")
@ComponentScan("$MODULE.dao.jpa")
@EnableJpaRepositories("$MODULE.dao.jpa.repository")
@EntityScan("$MODULE.po", "$MODULE.dto")
class ModuleConfiguration