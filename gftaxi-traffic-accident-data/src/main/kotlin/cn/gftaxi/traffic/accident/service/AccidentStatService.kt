package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import reactor.core.publisher.Flux
import java.time.Year
import java.time.YearMonth

/**
 * 事故登记 Service。
 *
 * @author RJ
 */
interface AccidentStatService {
  /**
   * 事故报告按月汇总统计。
   *
   * 1. 如果用户没有 [事故登记查阅相关角色][ROLES_REGISTER_READ]，
   *    则返回 [PermissionDeniedException] 类型的 [Flux.error]。
   * 2. 如果[统计的开始年月][from] 和 [结束年月][to] 跨度大于两年或开始年月大于结束年月，
   *    则返回 [IllegalArgumentException] 类型的 [Flux.error]。
   * 3. 返回结果按时间逆序排序。
   *
   * @param[from] 统计的开始年月，默认为当年的 1 月
   * @param[to]   统计的结束年月，默认为当年的 12 月
   */
  fun statRegisterMonthlySummary(from: YearMonth = YearMonth.of(Year.now().value, 1),
                                 to: YearMonth = YearMonth.of(Year.now().value, 12))
    : Flux<AccidentRegisterDto4StatSummary>

  /**
   * 事故报告按年汇总统计。
   *
   * 1. 如果用户没有 [事故登记查阅相关角色][ROLES_REGISTER_READ]，
   *    则返回 [PermissionDeniedException] 类型的 [Flux.error]。
   * 2. 如果[统计的开始年份][from] 和 [结束年份][to] 跨度大于两年或开始年份大于结束年份，
   *    则返回 [IllegalArgumentException] 类型的 [Flux.error]。
   * 3. 返回结果按时间逆序排序。
   *
   * @param[from] 统计的开始年份，默认为上年
   * @param[to]   统计的结束年份，默认为当年
   */
  fun statRegisterYearlySummary(from: Year = Year.now().minusYears(1),
                                to: Year = Year.now())
    : Flux<AccidentRegisterDto4StatSummary>

  /**
   * 事故报告按季度汇总统计。
   *
   * 1. 如果用户没有 [事故登记查阅相关角色][ROLES_REGISTER_READ]，
   *    则返回 [PermissionDeniedException] 类型的 [Flux.error]。
   * 2. 如果[统计的开始年份][from] 和 [结束年份][to] 跨度大于两年或开始年份大于结束年份，
   *    则返回 [IllegalArgumentException] 类型的 [Flux.error]。
   * 3. 返回结果按时间逆序排序。
   *
   * @param[from] 统计的开始年份，默认为当年
   * @param[to]   统计的结束年份，默认为当年
   */
  fun statRegisterQuarterlySummary(from: Year = Year.now(), to: Year = Year.now())
    : Flux<AccidentRegisterDto4StatSummary>
}