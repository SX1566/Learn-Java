package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
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
import java.util.*

fun random(start: Int, end: Int) = Random().nextInt(end + 1 - start) + start

/**
 * Test [AccidentRegisterServiceImpl.statSummary].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(
  AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class,
  ReactiveSecurityService::class
)
class StatSummaryMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val securityService: ReactiveSecurityService
) {
  @Test
  fun success() {
    // mock
    val dto = AccidentRegisterDto4StatSummary(
      scope = "本月",
      total = random(0, 100),
      checked = random(0, 100),
      checking = random(0, 100),
      drafting = random(0, 100),
      overdueDraft = random(0, 100),
      overdueRegister = random(0, 100)
    )
    val expected = listOf(dto, dto.copy(scope = "上月"), dto.copy(scope = "本年"))
    `when`(accidentRegisterDao.statSummary()).thenReturn(Flux.fromIterable(expected))
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.statSummary()

    // verify
    StepVerifier.create(actual)
      .expectNextSequence(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).statSummary()
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    StepVerifier.create(accidentRegisterService.statSummary())
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao, times(0)).statSummary()
  }
}