package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.ToCheck
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentRegisterServiceImpl.findTodo].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(
  AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class,
  ReactiveSecurityService::class
)
class FindTodoMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val securityService: ReactiveSecurityService
) {
  private fun randomTodoDto(code: String): AccidentRegisterDto4Todo {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Todo(code = code,
      carPlate = "粤A.00001",
      driverName = "driver1",
      driverType = Official,
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      hitForm = "车辆间事故",
      hitType = "追尾碰撞",
      location = "芳村上市路",
      motorcadeName = "一分一队",
      authorName = "小明",
      authorId = "Ming",
      draftTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      overdueDraft = false,
      registerTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      overdueRegister = false,
      submitTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset)
    )
  }

  @Test
  fun success() {
    findTodoByStatus(null)
    findTodoByStatus(Draft)
    findTodoByStatus(ToCheck)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    StepVerifier.create(accidentRegisterService.findTodo(null))
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao, times(0)).findTodo(null)
  }

  private fun findTodoByStatus(status: Status?) {
    Mockito.reset(securityService)
    Mockito.reset(accidentRegisterDao)
    // mock
    var code = 1
    val dto = randomTodoDto(code = "20180101_0$code")

    val expected = listOf(dto.copy(code = "20180101_0${++code}"), dto)
    `when`(accidentRegisterDao.findTodo(status)).thenReturn(Flux.fromIterable(expected))
    `when`(securityService.verifyHasAnyRole(*READ_ROLES)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.findTodo(status)

    // verify
    StepVerifier.create(actual)
      .expectNextSequence(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).findTodo(status)
  }
}