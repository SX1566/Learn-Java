package cn.gftaxi.traffic.accident.scheduler

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

private const val MODULE = "cn.gftaxi.traffic.accident.scheduler"

/**
 * All configuration for this module.
 *
 * @author RJ
 */
@Configuration("$MODULE.ModuleConfiguration")
@ComponentScan(MODULE)
@EnableScheduling
class ModuleConfiguration