package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.POUtils
import cn.gftaxi.traffic.accident.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.Utils.convert
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * Test [AccidentRegisterServiceImpl.get].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(
  AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class,
  ReactiveSecurityService::class
)
class GetMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: ReactiveSecurityService
) {
  @Test
  fun draftNotExists() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.empty())
    `when`(accidentRegisterDao.get(id)).thenReturn(Mono.empty())
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).get(id)
    verify(accidentDraftDao).get(id)
    verify(accidentRegisterDao, times(0)).createBy(any())
  }

  @Test
  fun draftExistsButNoRegister() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.empty())
    val draft = randomAccidentDraft(id = id)
    val registerPo = POUtils.randomAccidentRegister(draft)
    `when`(accidentRegisterDao.get(id)).thenReturn(Mono.empty())
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.just(draft))
    `when`(accidentRegisterDao.createBy(draft)).thenReturn(Mono.just(registerPo))

    // invoke
    val actual = accidentRegisterService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectNext(convert(registerPo, draft))
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).get(id)
    verify(accidentDraftDao).get(id)
    verify(accidentRegisterDao).createBy(draft)
  }

  @Test
  fun registerExists() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.empty())
    val draft = randomAccidentDraft(id = id)
    val registerPo = POUtils.randomAccidentRegister(draft = draft)
    `when`(accidentRegisterDao.get(id)).thenReturn(Mono.just(registerPo))
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.just(draft))

    // invoke
    val actual = accidentRegisterService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectNext(convert(registerPo, draft))
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).get(id)
    verify(accidentDraftDao).get(id)
    verify(accidentRegisterDao, times(0)).createBy(any())
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.error(PermissionDeniedException()))
    `when`(accidentRegisterDao.get(id)).thenReturn(Mono.empty())
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.get(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao, times(0)).get(id)
    verify(accidentDraftDao, times(0)).get(id)
    verify(accidentRegisterDao, times(0)).createBy(any())
  }
}