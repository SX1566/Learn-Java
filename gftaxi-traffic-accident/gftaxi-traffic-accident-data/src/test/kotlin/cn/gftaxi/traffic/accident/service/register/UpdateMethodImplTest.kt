package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_REGISTER_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4FormUpdate
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
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
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentRegisterServiceImpl.update].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class UpdateMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentRegisterService: AccidentRegisterService
) {
  private fun randomAccidentRegisterDto4FormUpdate(): AccidentRegisterDto4FormUpdate {
    return AccidentRegisterDto4FormUpdate()
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
    val dataDto = randomAccidentRegisterDto4FormUpdate()
    if (status != AuditStatus.ToSubmit)
      `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_MODIFY)).thenReturn(Mono.empty())
    else
      `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)).thenReturn(Mono.empty())
    `when`(accidentDao.getRegisterStatus(id)).thenReturn(status.toMono())
    `when`(accidentDao.update(
      id = id,
      data = dataDto.data,
      targetType = ACCIDENT_REGISTER_TARGET_TYPE,
      generateLog = true
    )).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.update(id, dataDto)

    // verify
    StepVerifier.create(actual).verifyComplete()
    if (status != AuditStatus.ToSubmit) // 限制为必须有修改权限
      verify(securityService).verifyHasAnyRole(ROLE_REGISTER_MODIFY)
    else                                // 提交或修改权限均可
      verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)
    verify(accidentDao).getRegisterStatus(id)
    verify(accidentDao).update(
      id = id,
      data = dataDto.data,
      targetType = ACCIDENT_REGISTER_TARGET_TYPE,
      generateLog = true
    )
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    `when`(accidentDao.getRegisterStatus(id)).thenReturn(Mono.empty())
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)).thenReturn(Mono.empty())
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_MODIFY)).thenReturn(Mono.empty())

    // invoke
    val dataDto = randomAccidentRegisterDto4FormUpdate()
    val actual = accidentRegisterService.update(id, dataDto)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(accidentDao).getRegisterStatus(id)
    verify(securityService, times(0)).verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)
    verify(securityService, times(0)).verifyHasAnyRole(ROLE_REGISTER_MODIFY)
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
      `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_MODIFY)).thenReturn(Mono.error(PermissionDeniedException()))
    else
      `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)).thenReturn(Mono.error(PermissionDeniedException()))
    `when`(accidentDao.getRegisterStatus(id)).thenReturn(status.toMono())

    // invoke
    val dataDto = randomAccidentRegisterDto4FormUpdate()
    val actual = accidentRegisterService.update(id, dataDto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    if (status != AuditStatus.ToSubmit) // 限制为必须有修改权限
      verify(securityService).verifyHasAnyRole(ROLE_REGISTER_MODIFY)
    else                                // 提交或修改权限均可
      verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)
    verify(accidentDao).getRegisterStatus(id)
    verify(accidentDao, times(0)).update(
      id = any(),
      data = any(),
      targetType = any(),
      generateLog = any()
    )
  }
}