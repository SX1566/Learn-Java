package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_CHECK
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.Utils.ACCIDENT_REGISTER_TARGET_TYPE
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomAttachment
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
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
 * Test [AccidentRegisterServiceImpl.checked].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class)
class CheckedMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentRegisterService: AccidentRegisterService
) {
  @Test
  fun `Success checked`() {
    success(true)  // 审核通过
    success(false) // 审核不通过
  }

  private fun success(passed: Boolean) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)

    // mock
    val id = 1
    val situation = randomCase(registerStatus = AuditStatus.ToCheck).second
    val checkedInfo = CheckedInfoDto(
      passed = passed,
      comment = "test",
      attachment = if (passed) null else randomAttachment()
    )
    val updateData = when (passed) {
      // 审核通过
      true -> mapOf(
        "registerStatus" to AuditStatus.Approved,
        "reportStatus" to AuditStatus.ToSubmit,
        "stage" to CaseStage.Following,
        "registerCheckedCount" to (situation.registerCheckedCount ?: 0) + 1,
        "registerCheckedComment" to null,
        "registerCheckedAttachments" to null
      )
      // 审核不通过
      else -> mapOf(
        "registerStatus" to AuditStatus.ToCheck,
        "registerCheckedCount" to (situation.registerCheckedCount ?: 0) + 1,
        "registerCheckedComment" to checkedInfo.comment,
        "registerCheckedAttachments" to checkedInfo.attachment?.run { listOf(checkedInfo.attachment) }
      )
    }
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_CHECK)).thenReturn(Mono.empty())
    `when`(accidentDao.getSituation(id)).thenReturn(situation.toMono())
    `when`(accidentDao.update(
      id = id,
      data = updateData,
      targetType = ACCIDENT_REGISTER_TARGET_TYPE,
      generateLog = false
    )).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.checked(id, checkedInfo)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_CHECK)
    verify(accidentDao).getSituation(id)
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
      .filterNot { it == AuditStatus.ToCheck }
      .forEach { failedByIllegalStatus(it) }
  }

  private fun failedByIllegalStatus(status: AuditStatus) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)

    // mock
    val id = 1
    val situation = randomCase(registerStatus = status).second
    val checkedInfo = CheckedInfoDto(passed = true)
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_CHECK)).thenReturn(Mono.empty())
    `when`(accidentDao.getSituation(id)).thenReturn(situation.toMono())

    // invoke
    val actual = accidentRegisterService.checked(id, checkedInfo)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_CHECK)
    verify(accidentDao).getSituation(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    val checkedInfo = CheckedInfoDto(passed = true)
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_CHECK)).thenReturn(Mono.empty())
    `when`(accidentDao.getSituation(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.checked(id, checkedInfo)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_CHECK)
    verify(accidentDao).getSituation(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    val dto = CheckedInfoDto(passed = true)
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_CHECK)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentRegisterService.checked(id, dto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_CHECK)
  }
}