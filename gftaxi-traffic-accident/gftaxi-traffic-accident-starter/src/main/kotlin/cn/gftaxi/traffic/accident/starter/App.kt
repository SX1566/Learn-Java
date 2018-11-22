package cn.gftaxi.traffic.accident.starter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
  scanBasePackages = ["tech.simter", "cn.gftaxi.bc", "cn.gftaxi.traffic.accident"],
  scanBasePackageClasses = [ProjectInfoAutoConfiguration::class]
)
class App

fun main(args: Array<String>) {
  runApplication<App>(*args)
}