package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.security.SecurityService
import java.time.OffsetDateTime
import java.util.*

fun random(start: Int, end: Int) = Random().nextInt(end + 1 - start) + start

/**
 * Test [AccidentRegisterServiceImpl].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentRegisterDao::class, SecurityService::class)
class AccidentRegisterServiceImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val securityService: SecurityService
) {
  @Test
  fun statSummaryWithRole() {
    // mock
    val dto = AccidentRegisterDto4StatSummary(
      scope = "本月",
      total = random(0, 100),
      checked = random(0, 100),
      checking = random(0, 100),
      drafting = random(0, 100),
      overdueDraft = random(0, 100),
      overdueRegister = random(0, 100)
    )
    val expected = listOf(dto, dto.copy(scope = "上月"), dto.copy(scope = "本年"))
    `when`(accidentRegisterDao.statSummary()).thenReturn(Flux.fromIterable(expected))
    doNothing().`when`(securityService).verifyHasAnyRole(*READ_ROLES)

    // invoke
    val actual = accidentRegisterService.statSummary()

    // verify
    StepVerifier.create(actual)
      .expectNextSequence(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).statSummary()
  }

  @Test
  fun statSummaryWithoutRole() {
    // mock
    doThrow(SecurityException()).`when`(securityService).verifyHasAnyRole(*READ_ROLES)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentRegisterService.statSummary().subscribe() })
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao, times(0)).statSummary()
  }

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
  fun findTodoWithRole() {
    findTodoByStatus(null)
    findTodoByStatus(Draft)
    findTodoByStatus(ToCheck)
  }

  @Test
  fun findTodoWithoutRole() {
    // mock
    doThrow(SecurityException()).`when`(securityService).verifyHasAnyRole(*READ_ROLES)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentRegisterService.findTodo(null).subscribe() })
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

  @Test
  fun findCheckedWithRole() {
    findCheckedByStatus(null)
    findCheckedByStatus(Approved)
    findCheckedByStatus(Rejected)
  }

  @Test
  fun findCheckedWithoutRole() {
    // mock
    doThrow(SecurityException()).`when`(securityService).verifyHasAnyRole(*READ_ROLES)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentRegisterService.findChecked(1, 25, null, null).subscribe() })
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao, times(0)).findChecked(1, 25, null, null)
  }

  private fun findCheckedByStatus(status: Status?) {
    Mockito.reset(securityService)
    Mockito.reset(accidentRegisterDao)
    // mock
    val pageNo = 1
    val pageSize = 25
    var code = 1
    val dto = randomCheckedDto(code = "20180101_0$code")

    val expectedRows = listOf(dto.copy(code = "20180101_0${++code}"), dto)
    val expected = PageImpl(expectedRows, PageRequest.of(pageNo, pageSize), expectedRows.size.toLong())
    `when`(accidentRegisterDao.findChecked(pageNo, pageSize, status, null))
      .thenReturn(Mono.just(expected))
    doNothing().`when`(securityService).verifyHasAnyRole(*READ_ROLES)

    // invoke
    val actual = accidentRegisterService.findChecked(pageNo, pageSize, status, null)

    // verify
    StepVerifier.create(actual)
      .expectNext(expected)
      .verifyComplete()
    verify(securityService).verifyHasAnyRole(*READ_ROLES)
    verify(accidentRegisterDao).findChecked(pageNo, pageSize, status, null)
  }

  private fun randomCheckedDto(code: String): AccidentRegisterDto4Checked {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Checked(
      code = code,
      carPlate = "粤A.00001",
      driverName = "driver1",
      driverType = Official,
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      checkedResult = Status.Approved,
      checkerName = "gftaxi",
      checkedCount = 1,
      checkedTime = OffsetDateTime.now()
    )
  }
}