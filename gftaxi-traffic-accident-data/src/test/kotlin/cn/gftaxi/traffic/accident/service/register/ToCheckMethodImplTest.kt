package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.ACCIDENT_REGISTER_TARGET_TYPE
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentRegisterServiceImpl.toCheck].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class)
class ToCheckMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentRegisterService: AccidentRegisterService
) {
  @Test
  fun `Success by allow status`() {
    successByAllowStatus(AuditStatus.ToSubmit)
    successByAllowStatus(AuditStatus.Rejected)
  }

  private fun successByAllowStatus(status: AuditStatus) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)

    // mock
    val id = 1
    val updateData = when (status) {
      // 首次提交
      AuditStatus.ToSubmit -> mapOf(
        "stage" to CaseStage.Registering,
        "draftStatus" to DraftStatus.Drafted,
        "registerStatus" to AuditStatus.ToCheck
      )
      // 非首次提交
      else -> mapOf("registerStatus" to AuditStatus.ToCheck)
    }
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getRegisterStatus(id)).thenReturn(status.toMono())
    `when`(accidentDao.update(
      id = id,
      data = updateData,
      targetType = ACCIDENT_REGISTER_TARGET_TYPE,
      generateLog = false
    )).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
    verify(accidentDao).getRegisterStatus(id)
    verify(accidentDao).update(
      id = id,
      data = updateData,
      targetType = ACCIDENT_REGISTER_TARGET_TYPE,
      generateLog = false
    )
  }

  @Test
  fun `Failed by illegal status`() {
    AuditStatus.values()
      .filterNot { it == AuditStatus.ToSubmit || it == AuditStatus.Rejected }
      .forEach { failedByIllegalStatus(it) }
  }

  private fun failedByIllegalStatus(status: AuditStatus) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)

    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getRegisterStatus(id)).thenReturn(status.toMono())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
    verify(accidentDao).getRegisterStatus(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getRegisterStatus(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
    verify(accidentDao).getRegisterStatus(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
  }
}