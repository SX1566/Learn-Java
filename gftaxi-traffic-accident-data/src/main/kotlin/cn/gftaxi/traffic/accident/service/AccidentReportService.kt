package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.ROLE_CHECK
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.ROLE_MODIFY
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.ROLE_SUBMIT
import cn.gftaxi.traffic.accident.po.AccidentReport.Status
import cn.gftaxi.traffic.accident.po.AccidentReport.Status.*
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import java.time.Year
import java.time.YearMonth

/**
 * 事故报告 Service。
 *
 * @author RJ
 * @author zh
 */
interface AccidentReportService {
  /**
   * 获取指定状态的案件分页信息。
   *
   * 返回的信息按事发时间逆序排序，模糊搜索车队、车号、司机、编号。
   *
   * @param[statuses] 案件状态，为 null 则不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   * @throws [PermissionDeniedException] 不是 [READ_ROLES] 中的任一角色之一
   */
  fun find(pageNo: Int = 1, pageSize: Int = 25, statuses: List<Status>? = null, search: String? = null)
    : Mono<Page<AccidentReportDto4View>>

  /**
   * 获取指定 ID 的事故报告信息。
   *
   * 1. 如果案件还没有报告过，则自动根据事故登记信息生成一条草稿状态的事故报告信息返回，
   * 2. 如果不是 [READ_ROLES] 中的任一角色之一，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 3. 如果案件不存在则返回 [NotFoundException] 类型的 [Mono.error]。
   *
   * @param[id] 事故 ID
   */
  fun get(id: Int): Mono<AccidentReportDto4Form>

  /**
   * 更新事故报告信息。
   *
   * 更新时要注意只更新那些与当前值不相同的数据，与当前值相同的数据忽略不处理。
   * 对于被更新了的数据，需要生成相应的操作记录，记录详细的更新日志。
   *
   * 权限控制规则如下：
   *
   * 1. 如果案件处于非待报告状态，只有有案件修改权限 [ROLE_MODIFY] 的用户才可以更新案件信息。
   * 2. 如果案件处于待报告状态，有案件报告权限 [ROLE_SUBMIT] 的用户也可以更新案件信息。
   *
   * 如果不符合上述的权限规则，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 如果案件不存在则返回 [NotFoundException] 类型的 [Mono.error]。
   *
   * @param[id] 要修改案件的 ID
   * @param[data] 要更新的信息，key 为 [AccidentRegisterDto4Form] 属性名，value 为该 DTO 相应的属性值，
   *              使用者只传入已改动的属性键值对，没有改动的属性不要传入来。
   *
   * @return 更新完毕的 [Mono] 信号
   */
  fun update(id: Int, data: Map<String, Any?>): Mono<Void>

  /**
   * 将待报告或审核不通过的事故报告信息提交审核。
   *
   * 1. 需要生成相应的操作记录。
   * 2. 如果案件不存在则返回 [NotFoundException] 类型的 [Mono.error]。
   * 3. 如果无 [ROLE_SUBMIT] 提交权限，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 4. 如果案件不是待报告 [Draft] 或审核不通过 [Rejected] 状态，则返回 [ForbiddenException] 类型的 [Mono.error]。
   *
   * @param[id] 案件 ID
   */
  fun toCheck(id: Int): Mono<Void>

  /**
   * 审核待审核状态的事故报告信息。
   *
   * 1. 需要生成相应的操作记录。
   * 2. 如果案件不存在则返回 [NotFoundException] 类型的 [Mono.error]。
   * 3. 如果无 [ROLE_CHECK] 审核权限，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   * 4. 如果案件不是待审核 [ToCheck] 状态，则返回 [ForbiddenException] 类型的 [Mono.error]。
   *
   * @param[id] 案件 ID
   * @param[checkedInfo] 审核信息
   * @return 审核完毕的 [Mono] 信号
   */
  fun checked(id: Int, checkedInfo: CheckedInfo): Mono<Void>

  /**
   * 事故报告按月汇总统计。
   *
   * 返回结果按时间逆序排序。
   *
   * 如果无 [READ_ROLES] 中的任一角色之一，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   *
   * 如果统计范围的开始点和结束点跨度大于两年或统计范围开始点大于结束点，
   * 则返回 [IllegalArgumentException] 类型的 [Mono.error]。
   *
   * @param[from] 统计的开始年月，默认为当年的 1 月
   * @param[to]   统计的结束年月，默认为当年的 12 月
   */
  fun statSummaryByMonthly(from: YearMonth? = YearMonth.of(Year.now().value, 1),
                           to: YearMonth? = YearMonth.of(Year.now().value, 12))
    : Flux<AccidentReportDto4StatSummary>

  /**
   * 事故报告按年汇总统计。
   *
   * 返回结果按年份逆序排序。
   *
   * 如果无 [READ_ROLES] 中的任一角色之一，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   *
   * 如果统计范围的开始点和结束点跨度大于两年或统计范围开始点大于结束点，
   * 则返回 [IllegalArgumentException] 类型的 [Mono.error]。
   *
   * @param[from] 统计的开始年份，默认为上年
   * @param[to]   统计的结束年份，默认为当年
   */
  fun statSummaryByYearly(from: Year? = Year.now().minusYears(1),
                          to: Year? = Year.now())
    : Flux<AccidentReportDto4StatSummary>

  /**
   * 事故报告按季度汇总统计。
   *
   * 返回结果按年份逆序排序。
   *
   * 如果无 [READ_ROLES] 中的任一角色之一，则返回 [PermissionDeniedException] 类型的 [Mono.error]。
   *
   * 如果统计范围的开始点和结束点跨度大于两年或统计范围开始点大于结束点，
   * 则返回 [IllegalArgumentException] 类型的 [Mono.error]。
   *
   * @param[from] 统计的开始年份，默认为当年
   * @param[to]   统计的结束年份，默认为当年
   */
  fun statSummaryByQuarterly(from: Year? = Year.now(), to: Year? = Year.now()): Flux<AccidentReportDto4StatSummary>
}