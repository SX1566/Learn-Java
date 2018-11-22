package cn.gftaxi.traffic.accident.dao.javamail

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

private const val MODULE = "cn.gftaxi.traffic.accident"

/**
 * All configuration for this module.
 *
 * @author JF
 */
@Configuration("$MODULE.dao.javamail.ModuleConfiguration")
@ComponentScan("$MODULE.dao.javamail")
class ModuleConfiguration