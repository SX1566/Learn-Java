package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.service.AccidentReportServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentReportServiceImpl.get].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentReportServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class)
class GetMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentReportService: AccidentReportService
) {
  @Test
  fun `Case not exists`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.empty())
    `when`(accidentDao.getReport(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentReportService.get(id)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
    verify(accidentDao).getReport(id)
  }

  @Test
  fun `Case exists`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.empty())
    val dto = AccidentReportDto4Form.from(randomCase(id = id))
    `when`(accidentDao.getReport(id)).thenReturn(Mono.just(dto))

    // invoke
    val actual = accidentReportService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectNext(dto)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
    verify(accidentDao).getReport(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentReportService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
  }
}