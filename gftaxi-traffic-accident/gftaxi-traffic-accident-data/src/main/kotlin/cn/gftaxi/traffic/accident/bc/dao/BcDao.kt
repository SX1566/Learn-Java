package cn.gftaxi.traffic.accident.bc.dao

import cn.gftaxi.traffic.accident.bc.dto.CaseRelatedInfoDto
import cn.gftaxi.traffic.accident.bc.dto.MotorcadeDto
import reactor.core.publisher.Flux
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

  /**
   * 获取车队信息列表
   *
   * 返回结果按 BC 系统分公司(bc_identity_actor.order_)正序 + 车队(bs_motorcade.code)正序排序。
   *
   * @param[includeDisabledStatus] 是否包含 `Disabled` 状态的车队，不指定默认仅返回 `Enabled` 状态的车队，
   *                                可指定为 `true` 返回 `Enabled` 和 `Disabled` 状态的车队。
   * @return 车队信息的 [Flux] 信号，无则返回 [Flux.empty]
   */
  fun findMotorcade(includeDisabledStatus: Boolean = false): Flux<MotorcadeDto>

  /**
   * 获取指定指定车号 [carPlate]、司机姓名 [driverName] 在指定日 [date] 对应的 BC 系统配置信息。
   *
   * 根据迁移记录、劳动合同等相关信息去获取。
   *
   * 如果指定的车号 [carPlate] 在 BC 系统中不存在，返回的信息中车辆相关的信息域的值为 null。
   * 如果指定的司机姓名 [driverName] 在 BC 系统中不存在，返回的信息中司机相关的信息域的值为 null。
   * 如果在 BC 系统找不到任何信息，返回相关信息域的值都为 null 的 [CaseRelatedInfoDto] 实例对象而不是[Mono.empty]。
   */
  fun getCaseRelatedInfo(carPlate: String, driverName: String, date: LocalDate): Mono<CaseRelatedInfoDto>
}