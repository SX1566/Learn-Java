package cn.gftaxi.traffic.accident.bc.dao.jdbc

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

private const val MODULE = "cn.gftaxi.traffic.accident.bc"

/**
 * All configuration for this module.
 *
 * @author RJ
 */
@Configuration("$MODULE.dao.jdbc.ModuleConfiguration")
@ComponentScan("$MODULE.dao.jdbc")
class ModuleConfiguration