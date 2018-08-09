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
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.security.SecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentRegisterServiceImpl.findTodo].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class, SecurityService::class)
class FindTodoMethodImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val securityService: SecurityService
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
      reportTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      overdueReport = false,
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
    doThrow(SecurityException()).`when`(securityService).verifyHasAnyRole(*READ_ROLES)

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
    doNothing().`when`(securityService).verifyHasAnyRole(*READ_ROLES)

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