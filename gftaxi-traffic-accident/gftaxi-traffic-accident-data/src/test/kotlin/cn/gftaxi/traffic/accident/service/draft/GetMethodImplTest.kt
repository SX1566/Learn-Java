package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_DRAFT_READ
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
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
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentDraftServiceImpl.get].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class GetMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentDraftService: AccidentDraftService
) {
  @Test
  fun `Case not exists`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)).thenReturn(Mono.empty())
    `when`(accidentDao.getDraft(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentDraftService.get(id)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_DRAFT_READ)
    verify(accidentDao).getDraft(id)
  }

  @Test
  fun `Case exists`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)).thenReturn(Mono.empty())
    val dto = AccidentDraftDto4Form.from(randomCase(id = id))
    `when`(accidentDao.getDraft(id)).thenReturn(Mono.just(dto))

    // invoke
    val actual = accidentDraftService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectNext(dto)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*ROLES_DRAFT_READ)
    verify(accidentDao).getDraft(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentDraftService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*ROLES_DRAFT_READ)
  }
}