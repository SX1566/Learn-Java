package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.Year
import java.time.YearMonth

/**
 * 事故登记 Service。
 *
 * @author RJ
 */
@Service
class AccidentStatServiceImpl @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentStatDao: AccidentStatDao
) : AccidentStatService {
  override fun statRegisterMonthlySummary(from: YearMonth, to: YearMonth): Flux<AccidentRegisterDto4StatSummary> {
    if (from.isAfter(to))
      return Flux.error(IllegalArgumentException("统计的开始年月不能大于结束年月！"))
    if (from.plusYears(2L).isBefore(to))
      return Flux.error(IllegalArgumentException("统计的开始年月和结束年月之间的跨度不能大于两年！"))
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .thenMany(Flux.defer { accidentStatDao.statRegisterMonthlySummary(from, to) })
  }

  override fun statRegisterYearlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary> {
    if (from.isAfter(to))
      return Flux.error(IllegalArgumentException("统计的开始年份不能大于结束年份！"))
    if (from.plusYears(2L).isBefore(to))
      return Flux.error(IllegalArgumentException("统计的开始年份和结束年份之间的跨度不能大于两年！"))
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .thenMany(Flux.defer { accidentStatDao.statRegisterYearlySummary(from, to) })
  }

  override fun statRegisterQuarterlySummary(from: Year, to: Year): Flux<AccidentRegisterDto4StatSummary> {
    if (from.isAfter(to))
      return Flux.error(IllegalArgumentException("统计的开始年份不能大于结束年份！"))
    if (from.plusYears(2L).isBefore(to))
      return Flux.error(IllegalArgumentException("统计的开始年份和结束年份之间的跨度不能大于两年！"))
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .thenMany(Flux.defer { accidentStatDao.statRegisterQuarterlySummary(from, to) })
  }
}