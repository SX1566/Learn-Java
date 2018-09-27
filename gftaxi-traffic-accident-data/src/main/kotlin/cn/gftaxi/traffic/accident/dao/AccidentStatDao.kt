package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import reactor.core.publisher.Flux
import java.time.Year
import java.time.YearMonth

/**
 * 事故统计 Dao。
 *
 * @author RJ
 */
interface AccidentStatDao {
  /**
   * 事故登记按月汇总统计。
   *
   * 返回结果按时间逆序排序。
   *
   * @param[from] 统计的开始年月
   * @param[to]   统计的结束年月
   */
  fun statRegisterMonthlySummary(from: YearMonth, to: YearMonth): Flux<AccidentRegisterDto4StatSummary>

  /**
   * 事故登记按年汇总统计。
   *
   * 返回结果按年份逆序排序。
   *
   * @param[from] 统计的开始年份
   * @param[to]   统计的结束年份
   */
  fun statRegisterYearlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary>

  /**
   * 事故登记按季度汇总统计。
   *
   * 返回结果按年份逆序排序。
   *
   * @param[from] 统计的开始年份
   * @param[to]   统计的结束年份
   */
  fun statRegisterQuarterlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary>
}