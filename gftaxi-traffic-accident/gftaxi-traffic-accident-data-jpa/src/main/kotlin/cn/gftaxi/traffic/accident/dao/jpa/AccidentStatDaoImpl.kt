package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage.Closed
import cn.gftaxi.traffic.accident.common.StatType
import cn.gftaxi.traffic.accident.common.StatType.*
import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4StatSummary
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故统计 Dao 实现。
 *
 * @author RJ
 * @author zh
 */
@Component
class AccidentStatDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager
) : AccidentStatDao {
  @Suppress("UNCHECKED_CAST")
  private fun statRegisterSummary(from: LocalDate, to: LocalDate, statType: StatType)
    : Flux<AccidentRegisterDto4StatSummary> {
    val isStatQuarterly = statType == Quarterly
    val formatQl = if (isStatQuarterly) {
      "extract(year from c.happen_time) || 'Q' || extract(Quarter from c.happen_time)"
    } else {
      "to_char(c.happen_time, :format)"
    }
    val ql = """
      select $formatQl as scope,
        count(0) as total,
        count(case when register_status = :Approved then 0 else null end) as checked,
        count(case when register_status in (:ToCheck, :Rejected) then 0 else null end) as checking,
        count(case when register_status in (:ToSubmit) then 0 else null end) as drafting,
        count(case when overdue_draft then 0 else null end) as overdue_draft,
        count(case when overdue_register then 0 else null end) as overdue_register
      from gf_accident_case as c
      inner join gf_accident_situation as s on c.id = s.id
      where c.happen_time >= :from and c.happen_time < :to
      group by scope
      order by scope desc
    """.trimIndent()
    return em.createNativeQuery(ql, AccidentRegisterDto4StatSummary::class.java)
      .setParameter("from", from)
      .setParameter("to", to)
      .apply {
        if (!isStatQuarterly) this.setParameter("format", statType.pattern)
      }
      .apply { AuditStatus.values().forEach { this.setParameter(it.name, it.value()) } }
      .let { Flux.fromIterable(it.resultList as List<AccidentRegisterDto4StatSummary>) }
  }

  @Suppress("UNCHECKED_CAST")
  private fun statReportSummary(from: LocalDate, to: LocalDate, statType: StatType)
    : Flux<AccidentReportDto4StatSummary> {
    val isStatQuarterly = statType == Quarterly
    val formatQl = if (isStatQuarterly) {
      "extract(year from c.happen_time) || 'Q' || extract(Quarter from c.happen_time)"
    } else {
      "to_char(c.happen_time, :format)"
    }
    val ql = """
      select $formatQl as scope,
        count(0) as total,
        count(case when stage = :Closed then 0 else null end) as closed,
        count(case when report_status = :Approved then 0 else null end) as checked,
        count(case when report_status in (:ToCheck, :Rejected) then 0 else null end) as checking,
        count(case when report_status in (:ToSubmit) then 0 else null end) as reporting,
        count(case when overdue_report then 0 else null end) as overdue_report
      from gf_accident_case as c
      inner join gf_accident_situation as s on c.id = s.id
      where c.happen_time >= :from and c.happen_time < :to and register_status = :Approved
      group by scope
      order by scope desc
    """.trimIndent()
    return em.createNativeQuery(ql, AccidentReportDto4StatSummary::class.java)
      .setParameter("from", from)
      .setParameter("to", to)
      .apply {
        if (!isStatQuarterly) this.setParameter("format", statType.pattern)
      }
      .setParameter(Closed.name, Closed.value())
      .apply { AuditStatus.values().forEach { this.setParameter(it.name, it.value()) } }
      .let { Flux.fromIterable(it.resultList as List<AccidentReportDto4StatSummary>) }
  }

  override fun statRegisterMonthlySummary(from: YearMonth, to: YearMonth): Flux<AccidentRegisterDto4StatSummary> {
    return statRegisterSummary(
      from = from.atDay(1),
      to = to.plusMonths(1).atDay(1),
      statType = Monthly
    )
  }

  override fun statRegisterYearlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary> {
    return statRegisterSummary(
      from = from.atDay(1),
      to = to.plusYears(1).atDay(1),
      statType = Yearly
    )
  }

  override fun statRegisterQuarterlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary> {
    return statRegisterSummary(
      from = from.atDay(1),
      to = to.plusYears(1).atDay(1),
      statType = Quarterly
    )
  }

  override fun statReportMonthlySummary(from: YearMonth, to: YearMonth): Flux<AccidentReportDto4StatSummary> {
    return statReportSummary(
      from = from.atDay(1),
      to = to.plusYears(1).atDay(1),
      statType = Monthly
    )
  }

  override fun statReportYearlySummary(from: Year, to: Year): Flux<AccidentReportDto4StatSummary> {
    return statReportSummary(
      from = from.atDay(1),
      to = to.plusYears(1).atDay(1),
      statType = Yearly
    )
  }

  override fun statReportQuarterlySummary(from: Year, to: Year): Flux<AccidentReportDto4StatSummary> {
    return statReportSummary(
      from = from.atDay(1),
      to = to.plusYears(1).atDay(1),
      statType = Quarterly
    )
  }
}