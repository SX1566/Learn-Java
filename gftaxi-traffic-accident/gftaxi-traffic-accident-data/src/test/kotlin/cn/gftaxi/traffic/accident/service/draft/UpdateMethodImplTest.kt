package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_DRAFT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_SUBMIT
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormUpdate
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
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
 * Test [AccidentDraftServiceImpl.update].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class UpdateMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentDraftService: AccidentDraftService
) {
  private fun randomAccidentDraftDto4FormUpdate(): AccidentDraftDto4FormUpdate {
    return AccidentDraftDto4FormUpdate()
  }

  @Test
  fun `Success by allow role and status`() {
    DraftStatus.values().forEach { successByAllowRoleAndStatus(it) }
  }

  private fun successByAllowRoleAndStatus(status: DraftStatus) {
    // reset
    reset(securityService)
    reset(accidentDao)

    // mock
    val id = 1
    val dataDto = randomAccidentDraftDto4FormUpdate()
    if (status != DraftStatus.ToSubmit)
      `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_MODIFY)).thenReturn(Mono.empty())
    else
      `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)).thenReturn(Mono.empty())
    `when`(accidentDao.getDraftStatus(id)).thenReturn(status.toMono())
    `when`(accidentDao.update(
      id = id,
      data = dataDto.data,
      targetType = ACCIDENT_DRAFT_TARGET_TYPE,
      generateLog = true
    )).thenReturn(Mono.empty())

    // invoke
    val actual = accidentDraftService.update(id, dataDto)

    // verify
    StepVerifier.create(actual).verifyComplete()
    if (status != DraftStatus.ToSubmit) // 限制为必须有修改权限
      verify(securityService).verifyHasAnyRole(ROLE_DRAFT_MODIFY)
    else                                // 上报或修改权限均可
      verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)
    verify(accidentDao).getDraftStatus(id)
    verify(accidentDao).update(
      id = id,
      data = dataDto.data,
      targetType = ACCIDENT_DRAFT_TARGET_TYPE,
      generateLog = true
    )
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)).thenReturn(Mono.empty())
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_MODIFY)).thenReturn(Mono.empty())
    `when`(accidentDao.getDraftStatus(id)).thenReturn(Mono.empty())

    // invoke
    val dataDto = randomAccidentDraftDto4FormUpdate()
    val actual = accidentDraftService.update(id, dataDto)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(accidentDao).getDraftStatus(id)
    verify(securityService, times(0)).verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)
    verify(securityService, times(0)).verifyHasAnyRole(ROLE_DRAFT_MODIFY)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    DraftStatus.values().forEach { failedByPermissionDenied(it) }
  }

  private fun failedByPermissionDenied(status: DraftStatus) {
    // reset
    reset(securityService)
    reset(accidentDao)

    // mock
    val id = 1
    if (status != DraftStatus.ToSubmit)
      `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_MODIFY)).thenReturn(Mono.error(PermissionDeniedException()))
    else
      `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)).thenReturn(Mono.error(PermissionDeniedException()))
    `when`(accidentDao.getDraftStatus(id)).thenReturn(status.toMono())

    // invoke
    val dataDto = randomAccidentDraftDto4FormUpdate()
    val actual = accidentDraftService.update(id, dataDto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    if (status != DraftStatus.ToSubmit) // 限制为必须有修改权限
      verify(securityService).verifyHasAnyRole(ROLE_DRAFT_MODIFY)
    else                                // 上报或修改权限均可
      verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)
    verify(accidentDao).getDraftStatus(id)
    verify(accidentDao, times(0)).update(
      id = any(),
      generateLog = any(),
      data = any(),
      targetType = any()
    )
  }
}