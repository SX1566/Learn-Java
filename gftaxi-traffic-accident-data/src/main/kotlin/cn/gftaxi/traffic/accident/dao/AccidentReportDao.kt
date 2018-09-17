package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentReport
import cn.gftaxi.traffic.accident.po.AccidentReport.Status
import cn.gftaxi.traffic.accident.po.AccidentReport.Status.*
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Year
import java.time.YearMonth

/**
 * 事故报告 Dao。
 *
 * @author RJ
 */
interface AccidentReportDao {
  /**
   * 获取指定状态的案件分页信息。
   *
   * 返回的信息按事发时间逆序排序，模糊搜索车队、车号、司机、编号。
   *
   * @param[statuses] 案件状态，为 null 则不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   */
  fun find(pageNo: Int = 1, pageSize: Int = 25, statuses: List<Status>? = null, search: String? = null)
    : Mono<Page<AccidentReportDto4View>>

  /**
   * 获取指定 [id] 的事故报告信息。
   *
   * @return 如果案件不存在则返回 [Mono.empty]
   */
  fun get(id: Int): Mono<AccidentReportDto4Form>

  /**
   * 根据事故报案信息生成一条草稿状态的事故报告信息。
   *
   */
  fun createBy(accidentDraft: AccidentDraft): Mono<AccidentReport>

  /**
   * 获取事故报告信息的当前状态。
   *
   * 如果指定的事故报告信息不存在，则返回 [Mono.empty]。
   */
  fun getStatus(id: Int): Mono<Status>

  /**
   * 提交事故报告信息。
   *
   * 将案件的状态更新为待审核状态，如果是首次提交（[AccidentReport.reportTime] 为 null 时）：
   * 1. 更新 [AccidentReport.reportTime] 为当前时间。
   * 2. 更新 [AccidentReport.overdueReport] 的值，确定是否逾期报告，
   *    逾期报告的阈值通过系统属性 `app.report-overdue-hours` 设置(默认为 48 小时)。
   *
   * 更新成功返回 Mono.just(true)，否则返回 Mono.just(false)
   * 更新成功是指真的更新为了新的状态，如果状态没有更新则返回 `Mono.just(false)`。
   * @param[id] 案件 ID
   */
  fun toCheck(id: Int): Mono<Void>

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
   * 更新事故报告信息。
   *
   * 更新成功返回 `Mono.just(true)`，否则返回 `Mono.just(false)`。
   * 更新成功是指真的更新了某些数据，如果没有修改任何数据则返回 `Mono.just(false)`。
   *
   * 更新说明详见 [AccidentRegisterDao.update] 的接口说明。
   *
   * @param[id] 案件 ID
   * @param[data] 要更新的信息，key 为 [AccidentReportDto4Form] 属性名，value 为该 DTO 相应的属性值。
   */
  fun update(id: Int, data: Map<String, Any?>): Mono<Boolean>

  /**
   * 事故报告按月汇总统计。
   *
   * 返回结果按时间逆序排序。
   *
   * @param[from] 统计的开始年月
   * @param[to]   统计的结束年月
   */
  fun statSummaryByMonthly(from: YearMonth, to: YearMonth): Flux<AccidentReportDto4StatSummary>

  /**
   * 事故报告按年汇总统计。
   *
   * 返回结果按年份逆序排序。
   *
   * @param[from] 统计的开始年份
   * @param[to]   统计的结束年份
   */
  fun statSummaryByYearly(from: Year, to: Year): Flux<AccidentReportDto4StatSummary>

  /**
   * 事故报告按季度汇总统计。
   *
   * 返回结果按年份逆序排序。
   *
   * @param[from] 统计的开始年份
   * @param[to]   统计的结束年份
   */
  fun statSummaryByQuarterly(from: Year, to: Year): Flux<AccidentReportDto4StatSummary>
}