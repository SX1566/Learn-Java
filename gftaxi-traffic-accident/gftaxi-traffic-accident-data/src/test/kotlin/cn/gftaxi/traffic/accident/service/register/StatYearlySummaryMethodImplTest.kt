package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
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
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.Year

/**
 * Test [AccidentStatServiceImpl.statRegisterYearlySummary].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentStatServiceImpl::class)
@MockBean(AccidentStatDao::class, ReactiveSecurityService::class, OperationService::class)
class StatYearlySummaryMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentStatDao: AccidentStatDao,
  private val accidentStatService: AccidentStatService
) {
  @Test
  fun success() {
    // mock
    val from = Year.of(2017)
    val to = Year.of(2018)
    val dto = AccidentRegisterDto4StatSummary(
      scope = "x",
      total = randomInt(0, 100),
      checked = randomInt(0, 100),
      checking = randomInt(0, 100),
      drafting = randomInt(0, 100),
      overdueDraft = randomInt(0, 100),
      overdueRegister = randomInt(0, 100)
    )
    val expected = listOf(dto.copy(scope = "${from.value}"), dto.copy(scope = "${to.value}"))
    `when`(accidentStatDao.statRegisterYearlySummary(from, to)).thenReturn(Flux.fromIterable(expected))
    `when`(securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentStatService.statRegisterYearlySummary(from, to)

    // verify
    StepVerifier.create(actual)
      .expectNextSequence(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_REGISTER_READ)
    verify(accidentStatDao).statRegisterYearlySummary(from, to)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val from = Year.of(2017)
    val to = Year.of(2018)
    `when`(securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    StepVerifier.create(accidentStatService.statRegisterYearlySummary(from, to))
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*ROLES_REGISTER_READ)
    verify(accidentStatDao, times(0)).statRegisterYearlySummary(from, to)
  }

  @Test
  fun `Failed by fromYearMonth is after toYearMonth`() {
    // mock
    val from = Year.of(2017)
    val to = from.minusYears(1)

    // invoke and verify
    StepVerifier.create(accidentStatService.statRegisterYearlySummary(from, to))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }

  @Test
  fun `Failed by more than two years`() {
    // mock
    val from = Year.of(2017)
    val to = from.plusYears(3)

    // invoke and verify
    StepVerifier.create(accidentStatService.statRegisterYearlySummary(from, to))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }
}