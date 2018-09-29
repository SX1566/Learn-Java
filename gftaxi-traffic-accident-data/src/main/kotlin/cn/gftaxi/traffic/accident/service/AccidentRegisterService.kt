package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_CHECK
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4View
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException

/**
 * 事故登记 Service。
 *
 * @author RJ
 */
interface AccidentRegisterService {
  /**
   * 获取指定条件的事故登记分页信息。
   *
   * 1. 如果用户没有 [事故登记查阅相关角色][ROLES_REGISTER_READ]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 模糊搜索 [事故编号][AccidentCase.code]、[事故车号][AccidentCase.carPlate]、[当事司机姓名][AccidentCase.driverName]。
   * 3. 返回结果按 [事发时间][AccidentCase.happenTime] 逆序排序，
   *
   * @param[registerStatuses] 事故登记状态，为空代表不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   */
  fun find(pageNo: Int = 1, pageSize: Int = 25, registerStatuses: List<AuditStatus>? = null, search: String? = null)
    : Mono<Page<AccidentRegisterDto4View>>

  /**
   * 获取指定 [主键][id] 的事故登记信息。
   *
   * 1. 如果用户没有 [事故登记查阅相关角色][ROLES_REGISTER_READ]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果案件不存在，则返回 [Mono.empty]。
   *
   */
  fun get(id: Int): Mono<AccidentRegisterDto4Form>

  /**
   * 更新指定 [主键][id] 的事故登记信息。
   *
   * 要更新的信息限制在动态 Bean [AccidentRegisterDto4FormUpdate] 允许的范围内。
   *
   * 1. 登记信息提交后只能由有 [事故登记修改角色][ROLE_REGISTER_MODIFY] 的用户执行更新，
   *    登记信息提交前有 [事故登记提交角色][ROLE_REGISTER_SUBMIT] 的用户也可以执行更新，
   *    否则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果案件不存在，则返回 [NotFoundException] 类型的 [Mono.error]。
   * 3. 生成更新日志。
   *
   * @return 更新完毕的 [Mono] 信号
   */
  fun update(id: Int, dataDto: AccidentRegisterDto4FormUpdate): Mono<Void>

  /**
   * 提交指定 [主键][id] 的事故登记信息给审核员审核。
   *
   * 1. 如果用户没有 [事故登记提交角色][ROLE_REGISTER_SUBMIT]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果案件不存在，则返回 [NotFoundException] 类型的 [Mono.error]。
   * 3. 如果案件的 [登记信息状态][AccidentSituation.registerStatus] 不是 [待提交][AuditStatus.ToSubmit]
   *    或 [审核不通过][AuditStatus.Rejected] 状态，则返回 [ForbiddenException] 类型的 [Mono.error]，
   *    否则设置 [登记信息状态][AccidentSituation.registerStatus] 为 [待审核][AuditStatus.ToCheck] 状态。
   * 4. 如果是首次提交登记信息，即案件的 [登记信息状态][AccidentSituation.registerStatus] = [待提交][AuditStatus.ToSubmit]
   *    则设置 [案件阶段标记][AccidentSituation.stage] 为 [登记处理中][CaseStage.Registering] 状态
   *    和设置 [报案信息状态][AccidentSituation.draftStatus] 为 [已登记][DraftStatus.Registering] 状态。
   * 5. 生成提交日志。
   *
   * @return 提交完毕的 [Mono] 信号
   */
  fun toCheck(id: Int): Mono<Void>

  /**
   * 审核指定 [主键][id] 的事故登记信息。
   *
   * 1. 如果用户没有 [事故登记审核角色][ROLE_REGISTER_CHECK]，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 2. 如果案件不存在，则返回 [NotFoundException] 类型的 [Mono.error]。
   * 3. 如果案件的 [登记信息状态][AccidentSituation.registerStatus] 不是 [待审核][AuditStatus.ToCheck] 状态
   *    则返回 [ForbiddenException] 类型的 [Mono.error]。
   * 4. 如果审核不通过，即 [CheckedInfoDto.passed] = false：
   *    - 设置 [登记信息状态][AccidentSituation.registerStatus] 为 [审核不通过][AuditStatus.Rejected] 状态。
   *    - 将 [登记信息审核次数][AccidentSituation.registerCheckedCount] 加 1。
   *    - 设置 [登记信息最后一次审核的审核意见][AccidentSituation.registerCheckedComment] = [CheckedInfoDto.comment]。
   *    - 设置 [登记信息最后一次审核的的审核附件][AccidentSituation.registerCheckedAttachments]。
   * 5. 如果审核通过，即 [CheckedInfoDto.passed] = true：
   *    - 设置 [登记信息状态][AccidentSituation.registerStatus] 为 [审核通过][AuditStatus.Approved] 状态。
   *    - 设置 [报告信息状态][AccidentSituation.reportStatus] 为 [待提交][AuditStatus.ToSubmit] 状态。
   *    - 设置 [案件阶段标记][AccidentSituation.stage] 为 [报告处理中][CaseStage.Reporting] 状态。
   *    - 将 [登记信息审核次数][AccidentSituation.registerCheckedCount] 加 1。
   *    - 设置 [登记信息最后一次审核的审核意见][AccidentSituation.registerCheckedComment] = [CheckedInfoDto.comment]。
   *    - 设置 [登记信息最后一次审核的的审核附件][AccidentSituation.registerCheckedAttachments]。
   * 5. 生成审核日志。
   *
   * @param[checkedInfo] 审核信息
   * @return 审核完毕的 [Mono] 信号
   */
  fun checked(id: Int, checkedInfo: CheckedInfoDto): Mono<Void>
}