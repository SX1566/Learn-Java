package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_DRAFT_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_SUBMIT
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormSubmit
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4View
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException

/**
 * 事故报案 Service。
 *
 * @author RJ
 */
interface AccidentDraftService {
  /**
   * 获取指定条件的事故报案分页信息。
   *
   * 1. 如果用户没有 [事故报案查阅相关角色][ROLES_DRAFT_READ]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 模糊搜索 [事故编号][AccidentCase.code]、[事故车号][AccidentCase.carPlate]、[当事司机姓名][AccidentCase.driverName]。
   * 3. 返回结果按 [事发时间][AccidentCase.happenTime] 逆序排序，
   *
   * @param[draftStatuses] -[事故报案状态][AccidentSituation.draftStatus]，为空代表不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   */
  fun find(pageNo: Int = 1, pageSize: Int = 25, draftStatuses: List<DraftStatus>? = null, search: String? = null)
    : Mono<Page<AccidentDraftDto4View>>

  /**
   * 获取指定 [主键][id] 的报案信息。
   *
   * 1. 如果用户没有 [事故报案查阅相关角色][ROLES_DRAFT_READ]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果案件不存，则返回 [Mono.empty]。
   */
  fun get(id: Int): Mono<AccidentDraftDto4Form>

  /**
   * 上报新的报案。
   *
   * 1. 如果用户没有 [事故报案上报角色][ROLE_DRAFT_SUBMIT]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果指定 [事故车号][AccidentCase.carPlate] 和 [事发时间][AccidentCase.happenTime] 的案件已经存在，
   *    则返回 [NonUniqueException] 类型的 [Mono.error]。
   * 3. 如果上报成功：
   *     - 生成相应的 [AccidentCase] 和 [AccidentSituation]，
   *       且设置 [AccidentSituation.stage] = [CaseStage.Drafting]、
   *       [AccidentSituation.draftStatus] = [DraftStatus.Drafting] 和
   *       [AccidentSituation.registerStatus] = [AuditStatus.ToSubmit]。
   *     - 根据 [事故车号][AccidentCase.carPlate] 自动识别车辆相关信息字段，并自动生成一条自车类型的 [当事车辆][AccidentCar] 信息。
   *     - 根据 [当事司机姓名][AccidentCase.driverName] 自动识别司机相关信息字段，并自动生成一条自车类型的 [当事人][AccidentPeople] 信息。
   *     - 生成上报日志。
   *     - 返回生成的案件信息。
   */
  fun submit(dto: AccidentDraftDto4FormSubmit): Mono<Pair<AccidentCase, AccidentSituation>>

  /**
   * 更新指定 [主键][id] 的报案信息。
   *
   * 要更新的信息限制在动态 Bean [AccidentDraftDto4FormUpdate] 允许的范围内。
   *
   * 1. 案件上报后只能由有 [事故报案修改角色][ROLE_DRAFT_MODIFY] 的用户执行更新，
   *    案件上报前有 [事故报案上报角色][ROLE_DRAFT_SUBMIT] 的用户也可以执行更新，
   *    否则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果案件不存在，则返回 [NotFoundException] 类型的 [Mono.error]。
   * 3. 生成更新日志。
   *
   * @return 更新完毕的 [Mono] 信号
   */
  fun update(id: Int, dataDto: AccidentDraftDto4FormUpdate): Mono<Void>
}