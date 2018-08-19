package cn.gftaxi.traffic.accident.dao

import reactor.core.publisher.Mono
import java.time.LocalDate

/**
 * BC 系统相关信息 Dao。
 *
 * @author RJ
 */
interface BcDao {
  /**
   * 获取指定车号 [carPlate] 在指定日 [date] 所属车队的名称。
   *
   * 如果车辆不存在或未分配车队则返回 `Mono.just("")`。
   */
  fun getMotorcadeName(carPlate: String, date: LocalDate): Mono<String>
}