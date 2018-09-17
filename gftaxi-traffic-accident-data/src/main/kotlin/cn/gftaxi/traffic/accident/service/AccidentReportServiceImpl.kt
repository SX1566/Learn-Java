package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentReportDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.dto.CheckedInfo
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentReport.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.Year
import java.time.YearMonth

/**
 * 事故报案 [AccidentReportService] 实现。
 *
 * @author zh
 * @author RJ
 */
@Service
@Transactional
class AccidentReportServiceImpl @Autowired constructor(
  private val dao: AccidentReportDao,
  private val securityService: ReactiveSecurityService
) : AccidentReportService {
  override fun find(pageNo: Int, pageSize: Int, statuses: List<Status>?, search: String?)
    : Mono<Page<AccidentReportDto4View>> {
    return securityService.verifyHasAnyRole(*READ_ROLES)
      .then(dao.find(pageNo = pageNo, pageSize = pageSize, statuses = statuses, search = search))
  }

  override fun get(id: Int): Mono<AccidentReportDto4Form> {
    TODO("not implemented")
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Void> {
    TODO("not implemented")
  }

  override fun toCheck(id: Int): Mono<Void> {
    TODO("not implemented")
  }

  override fun checked(id: Int, checkedInfo: CheckedInfo): Mono<Void> {
    TODO("not implemented")
  }

  override fun statSummaryByMonthly(from: YearMonth?, to: YearMonth?): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statSummaryByYearly(from: Year?, to: Year?): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented")
  }

  override fun statSummaryByQuarterly(from: Year?, to: Year?): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented")
  }
}