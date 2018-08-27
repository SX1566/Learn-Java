package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 事故登记 Dao。
 *
 * @author RJ
 */
interface AccidentRegisterDao {
  /**
   * 按本月、上月、本年的顺序获取事故登记汇总统计信息。
   *
   * 返回结果按"范围"的逆序排序
   *
   * @param scopeType 统计范围
   * @param from      统计范围开始点
   * @param to        统计范围结束点
   * @throws [IllegalArgumentException] 如果统计范围的开始点和结束点跨度大于两年或统计范围开始点大于结束点
   */
  fun statSummary(scopeType: ScopeType, from: Int?, to: Int?): Flux<AccidentRegisterDto4StatSummary>

  /**
   * 获取待登记、待审核案件信息。
   *
   * 返回结果按事发时间逆序排序。
   * 1. 当 [status] = [Draft] 时，返回处于待登记状态（即 [AccidentDraft.status] = [AccidentDraft.Status.Todo]）案件的相关信息。
   * 2. 当 [status] = [ToCheck] 时，返回处于待审核状态（即 [AccidentRegister.status] = [AccidentRegister.Status.ToCheck]）案件的相关信息。
   *
   * @param[status] 案件状态，只支持 [Draft] 和 [ToCheck] 两种状态，为 null 则返回这两种状态的案件
   * @throws [IllegalArgumentException] 如果指定的状态条件 [status] 不在允许的范围内
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
   * @throws [IllegalArgumentException] 如果指定的状态条件 [status] 不在允许的范围内
   */
  fun findLastChecked(pageNo: Int = 1, pageSize: Int = 25, status: Status? = null, search: String? = null)
    : Mono<Page<AccidentRegisterDto4LastChecked>>

  /**
   * 获取指定主键的事故登记信息。
   *
   * @return 如果案件不存在则返回 [Mono.empty]
   */
  fun get(id: Int): Mono<AccidentRegister>

  /**
   * 根据事故报案信息生成一条草稿状态的事故登记信息。
   *
   * 生成规则：
   * 1. 根据 [AccidentDraft.carPlate] 自动识别 [AccidentRegister] 的车辆相关信息，
   *    并自动生成一条自车类型的 [AccidentCar] 当事车辆信息。
   * 2. 根据 [AccidentDraft.driverName] 自动识别 [AccidentRegister] 的司机相关信息，
   *    并自动生成一条自车类型的 [AccidentPeople] 当事人信息。
   */
  fun createBy(accidentDraft: AccidentDraft): Mono<AccidentRegister>

  /**
   * 获取事故登记信息的当前状态。
   *
   * 如果指定的事故登记信息不存在，则返回 [Mono.empty]。
   */
  fun getStatus(id: Int): Mono<Status>

  /**
   * 提交事故登记信息。
   *
   * 将案件的状态更新为待审核状态，如果是首次提交（[AccidentRegister.registerTime] 为 null 时）：
   * 1. 更新 [AccidentRegister.registerTime] 为当前时间。
   * 2. 更新 [AccidentRegister.overdue] 的值，确定是否逾期登记，
   *    逾期登记的阈值通过系统属性 `app.register-overdue-hours` 设置(默认为 24 小时)。
   *
   * 更新成功返回 Mono.just(true)，否则返回 Mono.just(false)
   * 更新成功是指真的更新为了新的状态，如果状态没有更新则返回 `Mono.just(false)`。
   */
  fun toCheck(id: Int): Mono<Boolean>

  /**
   * 更新案件为指定的审核结果。
   *
   * 为安全起见，更新时要二次判断案件是否处于待审核 [ToCheck] 状态，不是则忽略不更新并返回 Mono.just(false)。
   *
   * 1. [passed] 为 true 时更新案件状态为审核通过 [Approved]。
   * 2. [passed] 为 false 时更新案件状态为审核不通过 [Rejected]。
   *
   * 更新成功返回 Mono.just(true)，否则返回 Mono.just(false)。
   * 更新成功是指真的更新为了新的状态，如果状态没有更新则返回 `Mono.just(false)`。
   */
  fun checked(id: Int, passed: Boolean): Mono<Boolean>

  /**
   * 更新事故登记信息。
   *
   * 更新成功返回 `Mono.just(true)`，否则返回 `Mono.just(false)`。
   * 更新成功是指真的更新了某些数据，如果没有修改任何数据则返回 `Mono.just(false)`。
   *
   * @param[id] 案件 ID
   * @param[data] 要更新的信息，key 为 [AccidentRegisterDto4Update] 属性名，value 为该 DTO 相应的属性值。
   */
  fun update(id: Int, data: Map<String, Any?>): Mono<Boolean>
}