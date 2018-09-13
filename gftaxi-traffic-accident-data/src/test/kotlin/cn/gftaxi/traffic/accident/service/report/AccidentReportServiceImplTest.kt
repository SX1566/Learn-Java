package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.dao.AccidentReportDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.service.AccidentReportServiceImpl
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

/**
 * Test [AccidentReportServiceImpl.find].
 *
 * @author zh
 */
@SpringJUnitConfig(AccidentReportServiceImpl::class)
@MockBean(AccidentReportDao::class, ReactiveSecurityService::class)
internal class AccidentReportServiceImplTest @Autowired constructor(
  private val accidentReportService: AccidentReportService,
  private val dao: AccidentReportDao,
  private val securityService: ReactiveSecurityService
) {
  private fun randomDto(id: Int): AccidentReportDto4View {
    return AccidentReportDto4View(id = id, code = randString(), driverType = AccidentRegister.DriverType.Official,
      happenTime = OffsetDateTime.now(), overdueDraft = true)
  }

  private fun randString(): String {
    return UUID.randomUUID().toString()
  }

  @Test
  fun success() {
    // mock
    val pageNo = 1
    val pageSize = 30
    val list = listOf(randomDto(1), randomDto(2))
    val page = PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.empty())
    `when`(dao.find(pageNo = pageNo, pageSize = pageSize)).thenReturn(Mono.just(page))

    // invoke and verify
    StepVerifier.create(accidentReportService.find(pageNo = pageNo, pageSize = pageSize))
      .expectNext(page)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(dao).find(pageNo = pageNo, pageSize = pageSize)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.error(PermissionDeniedException()))
    `when`(dao.find()).thenReturn(Mono.empty())

    // invoke and verify
    StepVerifier.create(accidentReportService.find())
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(dao).find()
  }
}