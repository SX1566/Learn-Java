package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.Utils.ACCIDENT_REPORT_TARGET_TYPE
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4FormUpdate
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.service.AccidentReportServiceImpl
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentReportServiceImpl.update].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentReportServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class)
class UpdateMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentReportService: AccidentReportService
) {
  private fun randomAccidentReportDto4FormUpdate(): AccidentReportDto4FormUpdate {
    return AccidentReportDto4FormUpdate()
  }

  @Test
  fun `Success by allow role and status`() {
    AuditStatus.values().forEach { successByAllowRoleAndStatus(it) }
  }

  private fun successByAllowRoleAndStatus(status: AuditStatus) {
    // reset
    reset(securityService)
    reset(accidentDao)

    // mock
    val id = 1
    val dataDto = randomAccidentReportDto4FormUpdate()
    if (status != AuditStatus.ToSubmit)
      `when`(securityService.verifyHasAnyRole(ROLE_REPORT_MODIFY)).thenReturn(Mono.empty())
    else
      `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)).thenReturn(Mono.empty())
    `when`(accidentDao.getReportStatus(id)).thenReturn(status.toMono())
    `when`(accidentDao.update(
      id = id,
      data = dataDto.data,
      targetType = ACCIDENT_REPORT_TARGET_TYPE,
      generateLog = true
    )).thenReturn(Mono.empty())

    // invoke
    val actual = accidentReportService.update(id, dataDto)

    // verify
    StepVerifier.create(actual).verifyComplete()
    if (status != AuditStatus.ToSubmit) // 限制为必须有修改权限
      verify(securityService).verifyHasAnyRole(ROLE_REPORT_MODIFY)
    else                                // 提交或修改权限均可
      verify(securityService).verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)
    verify(accidentDao).getReportStatus(id)
    verify(accidentDao).update(
      id = id,
      data = dataDto.data,
      targetType = ACCIDENT_REPORT_TARGET_TYPE,
      generateLog = true
    )
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    `when`(accidentDao.getReportStatus(id)).thenReturn(Mono.empty())
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)).thenReturn(Mono.empty())
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_MODIFY)).thenReturn(Mono.empty())

    // invoke
    val dataDto = randomAccidentReportDto4FormUpdate()
    val actual = accidentReportService.update(id, dataDto)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(accidentDao).getReportStatus(id)
    verify(securityService, times(0)).verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)
    verify(securityService, times(0)).verifyHasAnyRole(ROLE_REPORT_MODIFY)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    AuditStatus.values().forEach { failedByPermissionDenied(it) }
  }

  private fun failedByPermissionDenied(status: AuditStatus) {
    // reset
    reset(securityService)
    reset(accidentDao)

    // mock
    val id = 1
    if (status != AuditStatus.ToSubmit)
      `when`(securityService.verifyHasAnyRole(ROLE_REPORT_MODIFY)).thenReturn(Mono.error(PermissionDeniedException()))
    else
      `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)).thenReturn(Mono.error(PermissionDeniedException()))
    `when`(accidentDao.getReportStatus(id)).thenReturn(status.toMono())

    // invoke
    val dataDto = randomAccidentReportDto4FormUpdate()
    val actual = accidentReportService.update(id, dataDto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    if (status != AuditStatus.ToSubmit) // 限制为必须有修改权限
      verify(securityService).verifyHasAnyRole(ROLE_REPORT_MODIFY)
    else                                // 提交或修改权限均可
      verify(securityService).verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)
    verify(accidentDao).getReportStatus(id)
    verify(accidentDao, times(0)).update(
      id = any(),
      data = any(),
      targetType = any(),
      generateLog = any()
    )
  }
}