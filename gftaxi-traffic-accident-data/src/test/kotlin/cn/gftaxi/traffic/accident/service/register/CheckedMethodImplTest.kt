package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.CheckedInfo
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.Approval
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.Rejection
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType.Register
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_CHECK
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.ToCheck
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.security.SecurityService

/**
 * Test [AccidentRegisterServiceImpl.checked].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class, SecurityService::class)
class CheckedMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val accidentOperationDao: AccidentOperationDao,
  private val securityService: SecurityService
) {
  @Test
  fun success() {
    success(true)  // 审核通过
    success(false) // 审核不通过
  }

  private fun success(passed: Boolean) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentOperationDao)
    Mockito.reset(accidentRegisterDao)

    // mock
    val id = 1
    val dto = CheckedInfo(passed = passed)
    doNothing().`when`(securityService).verifyHasAnyRole(ROLE_CHECK)
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.just(ToCheck))
    `when`(accidentRegisterDao.checked(id, dto.passed)).thenReturn(Mono.just(true))
    val operationType = if (passed) Approval else Rejection
    `when`(accidentOperationDao.create(operationType = operationType, targetType = Register, targetId = id))
      .thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.checked(id, dto)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_CHECK)
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao).checked(id, dto.passed)
    verify(accidentOperationDao).create(operationType = operationType, targetType = Register, targetId = id)
  }

  @Test
  fun failedByIllegalStatus() {
    Status.values()
      .filter { it != ToCheck }
      .forEach { failedByIllegalStatus(it) }
  }

  private fun failedByIllegalStatus(status: Status) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentOperationDao)
    Mockito.reset(accidentRegisterDao)

    // mock
    val id = 1
    val dto = CheckedInfo(passed = true)
    doNothing().`when`(securityService).verifyHasAnyRole(ROLE_CHECK)
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.just(status))

    // invoke
    val actual = accidentRegisterService.checked(id, dto)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_CHECK)
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao, times(0)).checked(id, dto.passed)
    verify(accidentOperationDao, times(0)).create(any(), any(), any(), any(), any(), any(), any())
  }

  @Test
  fun failedByNotFound() {
    // mock
    val id = 1
    val dto = CheckedInfo(passed = true)
    doNothing().`when`(securityService).verifyHasAnyRole(ROLE_CHECK)
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.checked(id, dto)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_CHECK)
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao, times(0)).checked(id, dto.passed)
    verify(accidentOperationDao, times(0)).create(any(), any(), any(), any(), any(), any(), any())
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val id = 1
    val dto = CheckedInfo(passed = true)
    doThrow(SecurityException()).`when`(securityService).verifyHasAnyRole(ROLE_CHECK)

    // invoke
    val actual = accidentRegisterService.checked(id, dto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_CHECK)
    verify(accidentRegisterDao, times(0)).getStatus(id)
    verify(accidentRegisterDao, times(0)).checked(id, dto.passed)
    verify(accidentOperationDao, times(0)).create(any(), any(), any(), any(), any(), any(), any())
  }
}