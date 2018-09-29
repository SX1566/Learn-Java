package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4StatSummary
import cn.gftaxi.traffic.accident.service.AccidentStatService
import cn.gftaxi.traffic.accident.service.AccidentStatServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.YearMonth

/**
 * Test [AccidentStatServiceImpl.statReportMonthlySummary].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentStatServiceImpl::class)
@MockBean(AccidentStatDao::class, ReactiveSecurityService::class)
class StatMonthlySummaryMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentStatDao: AccidentStatDao,
  private val accidentStatService: AccidentStatService
) {
  @Test
  fun success() {
    // mock
    val from = YearMonth.of(2018, 1)
    val to = YearMonth.of(2018, 2)
    val dto = AccidentReportDto4StatSummary(
      scope = "x",
      total = randomInt(0, 100),
      checked = randomInt(0, 100),
      checking = randomInt(0, 100),
      reporting = randomInt(0, 100),
      overdueReport = randomInt(0, 100),
      closed = randomInt(0, 100)
    )
    val expected = listOf(dto.copy(scope = "201802"), dto.copy(scope = "201801"))
    `when`(accidentStatDao.statReportMonthlySummary(from, to)).thenReturn(Flux.fromIterable(expected))
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentStatService.statReportMonthlySummary(from, to)

    // verify
    StepVerifier.create(actual)
      .expectNextSequence(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
    verify(accidentStatDao).statReportMonthlySummary(from, to)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val from = YearMonth.of(2018, 1)
    val to = YearMonth.of(2018, 12)
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    StepVerifier.create(accidentStatService.statReportMonthlySummary(from, to))
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
    verify(accidentStatDao, times(0)).statReportMonthlySummary(from, to)
  }

  @Test
  fun `Failed by fromYearMonth is after toYearMonth`() {
    // mock
    val from = YearMonth.of(2018, 1)
    val to = from.minusMonths(1)

    // invoke and verify
    StepVerifier.create(accidentStatService.statReportMonthlySummary(from, to))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }

  @Test
  fun `Failed by more than two years`() {
    // mock
    val from = YearMonth.of(2018, 1)
    val to = from.plusYears(3)

    // invoke and verify
    StepVerifier.create(accidentStatService.statReportMonthlySummary(from, to))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }
}