package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
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
  /** 按本月、上月、本年的顺序获取事故登记汇总统计信息。 */
  fun statSummary(): Flux<AccidentRegisterDto4StatSummary>

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
  fun findChecked(pageNo: Int = 1, pageSize: Int = 25, status: Status? = null, search: String? = null)
    : Mono<Page<AccidentRegisterDto4Checked>>

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
}