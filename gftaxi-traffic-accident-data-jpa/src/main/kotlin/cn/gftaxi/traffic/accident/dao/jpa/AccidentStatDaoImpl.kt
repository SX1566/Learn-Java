package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
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
}