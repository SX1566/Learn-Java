package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4StatSummary
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Year
import java.time.YearMonth

/**
 * 事故统计 Dao 实现。
 *
 * @author RJ
 */
@Component
class AccidentStatDaoImpl : AccidentStatDao {
  override fun statRegisterMonthlySummary(from: YearMonth, to: YearMonth): Flux<AccidentRegisterDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statRegisterYearlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statRegisterQuarterlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statReportMonthlySummary(from: YearMonth, to: YearMonth): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statReportYearlySummary(from: Year, to: Year): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statReportQuarterlySummary(from: Year, to: Year): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented")
  }
}