package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.bc.dto.MotorcadeDto
import reactor.core.publisher.Flux

/**
 * 事故通用接口 Service。
 *
 * @author jw
 */
interface AccidentCommonService {
  /**
   * 获取车队信息列表
   *
   * 如果用户没有 [事故报告查阅相关角色][ROLES_REPORT_READ] 或 [事故登记查阅相关角色][ROLES_REGISTER_READ]，
   * 则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   *
   * 返回结果按 BC 系统分公司(bc_identity_actor.order_)正序 + 车队(bs_motorcade.code)正序排序。
   *
   * @param[includeDisabledStatus] 是否包含 `Disabled` 状态的车队，不指定默认仅返回 `Enabled` 状态的车队，
   *                                可指定为 `true` 返回 `Enabled` 和 `Disabled` 状态的车队。
   * @return 车队信息的 [Flux] 信号，无则返回 [Flux.empty]
   */
  fun findMotorcade(includeDisabledStatus: Boolean = false): Flux<MotorcadeDto>
}