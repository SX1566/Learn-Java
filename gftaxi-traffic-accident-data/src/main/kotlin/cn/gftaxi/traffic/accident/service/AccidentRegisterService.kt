package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_CHECK
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_MODIFY
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_READ
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_SUBMIT
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 事故登记 Service。
 *
 * @author RJ
 */
interface AccidentRegisterService {
  /** 按本月、上月、本年的顺序获取事故登记汇总统计信息。 */
  fun statSummary(): Flux<AccidentRegisterDto4StatSummary>

  /**
   * 获取待登记、待审核案件信息。
   *
   * @param[status] 案件状态，只支持 [Draft] 和 [ToCheck] 两种状态，为 null 则返回这两种状态的案件
   * @throws [SecurityException] 不是 [ROLE_READ]、[ROLE_SUBMIT]、[ROLE_MODIFY]、[ROLE_CHECK] 任一角色之一
   * @throws [IllegalArgumentException] 如果指定的状态条件 [status] 不在允许的范围内
   */
  fun findTodo(status: Status?): Flux<AccidentRegisterDto4Todo>

  /**
   * 获取已审核案件信息。
   *
   * @param[status] 案件状态，只支持 [Approved] 和 [Rejected] 两种状态，为 null 则返回这两种状态的案件
   * @param[search] 模糊搜索的条件值，为空则忽略
   * @throws [SecurityException] 不是 [ROLE_READ]、[ROLE_SUBMIT]、[ROLE_MODIFY]、[ROLE_CHECK] 任一角色之一
   * @throws [IllegalArgumentException] 如果指定的状态条件 [status] 不在允许的范围内
   */
  fun findChecked(pageNo: Int, pageSize: Int, status: Status?, search: String?): Mono<Page<AccidentRegisterDto4Checked>>
}