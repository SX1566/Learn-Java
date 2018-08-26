package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_MODIFY
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_SUBMIT
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
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
import tech.simter.reactive.context.SystemContext.User
import tech.simter.reactive.security.ReactiveSecurityService
import java.util.*

/**
 * Test [AccidentRegisterServiceImpl.update].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(
  AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class,
  ReactiveSecurityService::class
)
class UpdateMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val accidentOperationDao: AccidentOperationDao,
  private val securityService: ReactiveSecurityService
) {
  @Test
  fun successByAllowPermissionAndStatus() {
    // 1. 有修改权限时，可以修改任意状态
    Status.values().forEach {
      successByAllowPermissionAndStatus(hasSubmitRole = false, hasModifyRole = true, status = it)
      successByAllowPermissionAndStatus(hasSubmitRole = true, hasModifyRole = true, status = it)
    }

    // 2. 仅有案件登记权限时，只可以修改待登记状态的案件
    successByAllowPermissionAndStatus(hasSubmitRole = true, hasModifyRole = false, status = Draft)
  }

  private fun successByAllowPermissionAndStatus(hasSubmitRole: Boolean, hasModifyRole: Boolean, status: Status) {
    // reset
    reset(securityService)
    reset(accidentOperationDao)
    reset(accidentRegisterDao)

    // mock
    val id = 1
    val user = Optional.of(User(id = 0, account = "tester", name = "Tester"))
    val data = mapOf<String, Any?>()
    `when`(securityService.hasRole(ROLE_SUBMIT, ROLE_MODIFY)).thenReturn(Mono.just(Pair(hasSubmitRole, hasModifyRole)))
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(user))
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.just(status))
    `when`(accidentRegisterDao.update(id, data)).thenReturn(Mono.just(true))
    `when`(accidentOperationDao.create(any())).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.update(id, data)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).hasRole(ROLE_SUBMIT, ROLE_MODIFY)
    verify(securityService).getAuthenticatedUser()
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao).update(id, data)
    verify(accidentOperationDao).create(any())
  }

  @Test
  fun failedByNotFound() {
    // mock
    val id = 1
    `when`(securityService.hasRole(ROLE_SUBMIT, ROLE_MODIFY)).thenReturn(Mono.just(Pair(true, true)))
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.empty())

    // invoke
    val data = mapOf<String, Any?>()
    val actual = accidentRegisterService.update(id, data)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).hasRole(ROLE_SUBMIT, ROLE_MODIFY)
    verify(accidentRegisterDao).getStatus(id)
  }

  @Test
  fun failedByPermissionDenied() {
    // 1. 无任何权限
    Status.values().forEach {
      failedByPermissionDenied(hasSubmitRole = false, status = it)
    }

    // 2. 仅有案件登记权限但案件处于非待登记状态
    Status.values().filter { it != Draft }.forEach {
      failedByPermissionDenied(hasSubmitRole = true, status = it)
    }
  }

  private fun failedByPermissionDenied(hasSubmitRole: Boolean, status: Status) {
    // reset
    reset(securityService)
    reset(accidentRegisterDao)

    // mock
    val id = 1
    `when`(securityService.hasRole(ROLE_SUBMIT, ROLE_MODIFY)).thenReturn(Mono.just(Pair(hasSubmitRole, false)))
    `when`(accidentRegisterDao.getStatus(id)).thenReturn(Mono.just(status))

    // invoke
    val data = mapOf<String, Any?>()
    val actual = accidentRegisterService.update(id, data)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).hasRole(ROLE_SUBMIT, ROLE_MODIFY)
    verify(accidentRegisterDao).getStatus(id)
    verify(accidentRegisterDao, times(0)).update(any(), any())
  }
}