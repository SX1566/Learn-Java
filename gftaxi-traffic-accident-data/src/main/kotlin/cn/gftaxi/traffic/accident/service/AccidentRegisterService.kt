package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException

/**
 * 事故登记 Service。
 *
 * @author RJ
 */
interface AccidentRegisterService {
  /**
   * 按本月、上月、本年的顺序获取事故登记汇总统计信息。
   *
   * @throws [PermissionDeniedException] 不是 [READ_ROLES] 中的任一角色之一
   */
  fun statSummary(): Flux<AccidentRegisterDto4StatSummary>

  /**
   * 获取待登记、待审核案件信息。
   *
   * 返回结果按事发时间逆序排序。
   * 1. 当 [status] = [Draft] 时，返回处于待登记状态（即 [AccidentDraft.status] = [AccidentDraft.Status.Todo]）案件的相关信息。
   * 2. 当 [status] = [ToCheck] 时，返回处于待审核状态（即 [AccidentRegister.status] = [AccidentRegister.Status.ToCheck]）案件的相关信息。
   *
   * @param[status] 案件状态，只支持 [Draft] 和 [ToCheck] 两种状态，为 null 则返回这两种状态的案件
   * @throws [ForbiddenException] 如果指定的状态条件 [status] 不在允许的范围内
   * @throws [PermissionDeniedException] 不是 [READ_ROLES] 中的任一角色之一
   */
  fun findTodo(status: Status? = null): Flux<AccidentRegisterDto4Todo>

  /**
   * 获取已审核案件的最后审核信息。
   *
   * 返回的信息按事发时间逆序排序，模糊搜索编号、车号。
   * 1. 当 [status] = [Approved] 时，返回处于审核通过状态（即 [AccidentRegister.status] = [AccidentRegister.Status.Approved]）案件的相关信息。
   * 2. 当 [status] = [Rejected] 时，返回处于审核不通过状态（即 [AccidentRegister.status] = [AccidentRegister.Status.Rejected]）案件的相关信息。
   *
   * @param[status] 案件状态，只支持 [Approved] 和 [Rejected] 两种状态，为 null 则返回这两种状态的案件
   * @param[search] 模糊搜索的条件值，为空则忽略
   * @throws [ForbiddenException] 如果指定的状态条件 [status] 不在允许的范围内
   * @throws [PermissionDeniedException] 不是 [READ_ROLES] 中的任一角色之一
   */
  fun findChecked(pageNo: Int = 1, pageSize: Int = 25, status: Status? = null, search: String? = null)
    : Mono<Page<AccidentRegisterDto4Checked>>

  /**
   * 获取指定 ID 的事故登记信息。
   *
   * 如果事故报案信息还没有登记过，则自动根据事故报案信息生成一条草稿状态的事故登记信息返回，
   * 否则直接返回已有的事故登记信息。通过事故报案生成生成事故登记信息的规则：
   * 1. 根据 [AccidentDraft.carPlate] 自动识别 [AccidentRegister] 的车辆相关信息，
   *    并自动生成一条自车类型的 [AccidentCar] 当事车辆信息。
   * 2. 根据 [AccidentDraft.driverName] 自动识别 [AccidentRegister] 的司机相关信息，
   *    并自动生成一条自车类型的 [AccidentPeople] 当事人信息。
   *
   * @param[id] 事故 ID
   * @throws [NotFoundException] 案件不存在
   * @throws [PermissionDeniedException] 不是 [READ_ROLES] 中的任一角色之一
   */
  fun get(id: Int): Mono<AccidentRegisterDto4Form>

  /**
   * 更新事故登记信息。
   *
   * 更新时要注意只更新那些与当前值不相同的数据，与当前值相同的数据忽略不处理。
   * 对于被更新了的数据，需要生成相应的 [AccidentOperation] 操作记录，记录详细的更新日志。
   *
   * @param[id] 要修改案件的 ID
   * @param[data] 要更新的信息，key 为 [AccidentRegister] 的属性名，value 为相应的属性值
   * @throws [NotFoundException] 案件不存在
   * @throws [PermissionDeniedException] 无 [AccidentDraft.ROLE_MODIFY] 修改事故登记信息权限
   * @return 更新完毕的 [Mono] 信号
   */
  fun update(id: Int, data: Map<String, Any?>): Mono<Void>

  /**
   * 将待登记或审核不通过的事故登记信息提交审核。
   *
   * 需要生成相应的 [AccidentOperation] 操作记录。
   *
   * @param[id] 案件 ID
   * @throws [NotFoundException] 案件不存在
   * @throws [ForbiddenException] 案件不是待登记 [Draft] 或审核不通过 [Rejected] 状态
   * @throws [PermissionDeniedException] 无 [AccidentRegister.ROLE_SUBMIT] 提交权限
   * @return 提交完毕的 [Mono] 信号
   */
  fun toCheck(id: Int): Mono<Void>

  /**
   * 审核待审核状态的事故登记信息。
   *
   * 需要生成相应的 [AccidentOperation] 操作记录。
   *
   * @param[id] 案件 ID
   * @throws [NotFoundException] 案件不存在
   * @throws [ForbiddenException] 案件不是待审核 [ToCheck] 状态
   * @throws [PermissionDeniedException] 无 [AccidentRegister.ROLE_CHECK] 审核权限
   * @return 审核完毕的 [Mono] 信号
   */
  fun checked(id: Int): Mono<Void>
}