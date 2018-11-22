package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.service.AccidentReportServiceImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Page
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentReportServiceImpl.find].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentReportServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class FindMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentReportService: AccidentReportService
) {
  @Test
  fun success() {
    // mock
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.empty())
    val expected = Page.empty<AccidentReportDto4View>()
    `when`(accidentDao.findReport()).thenReturn(Mono.just(expected))

    // invoke
    val actual = accidentReportService.find()

    // verify
    StepVerifier.create(actual)
      .expectNext(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
    verify(accidentDao).findReport()
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    StepVerifier.create(accidentReportService.find())
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ)
  }
}