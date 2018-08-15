package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_SUBMIT
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
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
import tech.simter.reactive.context.SystemContext.User
import tech.simter.reactive.security.ReactiveSecurityService
import java.util.*

/**
 * Test [AccidentRegisterServiceImpl.toCheck].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(
  AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class,
  ReactiveSecurityService::class
)
class ToCheckMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val accidentOperationDao: AccidentOperationDao,
  private val securityService: ReactiveSecurityService
) {
  @Test
  fun successByAllowStatus() {
    successByAllowStatus(Draft)
    successByAllowStatus(Rejected)
  }

  private fun successByAllowStatus(status: Status) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentOperationDao)
    Mockito.reset(accidentRegisterDao)

    // mock
    val id = 1
    val user = Optional.of(User(id = 0, account = "tester", name = "Tester"))
    `when`(securityService.verifyHasAnyRole(ROLE_SUBMIT)).thenReturn(Mono.empty())
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(user))
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.just(status))
    `when`(accidentRegisterDao.toCheck(id)).thenReturn(Mono.just(true))
    `when`(accidentOperationDao.create(any())).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_SUBMIT)
    verify(securityService).getAuthenticatedUser()
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao).toCheck(id)
    verify(accidentOperationDao).create(any())
  }

  @Test
  fun failedByIllegalStatus() {
    Status.values()
      .filter { it != Draft && it != Rejected }
      .forEach { failedByIllegalStatus(it) }
  }

  private fun failedByIllegalStatus(status: Status) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentOperationDao)
    Mockito.reset(accidentRegisterDao)

    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.just(status))

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_SUBMIT)
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao, times(0)).toCheck(id)
    verify(accidentOperationDao, times(0)).create(any())
  }

  @Test
  fun failedByNotFound() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_SUBMIT)
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao, times(0)).toCheck(id)
    verify(accidentOperationDao, times(0)).create(any())
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_SUBMIT)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_SUBMIT)
    verify(accidentRegisterDao, times(0)).getStatus(id)
    verify(accidentRegisterDao, times(0)).toCheck(id)
    verify(accidentOperationDao, times(0)).create(any())
  }
}