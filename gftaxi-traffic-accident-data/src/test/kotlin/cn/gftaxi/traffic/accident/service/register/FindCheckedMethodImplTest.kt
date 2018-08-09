package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.PermissionDeniedException
import tech.simter.security.SecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentRegisterServiceImpl.findChecked].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentRegisterDao::class, AccidentDraftDao::class, AccidentOperationDao::class, SecurityService::class)
class AccidentRegisterServiceImplTest @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val securityService: SecurityService
) {
  private fun randomCheckedDto(code: String): AccidentRegisterDto4Checked {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Checked(
      code = code,
      carPlate = "粤A.00001",
      driverName = "driver1",
      driverType = Official,
      location = "虚拟地址",
      motorcadeName = "一分一队",
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      checkerName = "gftaxi",
      checkedCount = 1,
      checkedTime = OffsetDateTime.now()
    )
  }

  @Test
  fun success() {
    findCheckedByStatus(null)
    findCheckedByStatus(Approved)
    findCheckedByStatus(Rejected)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    doThrow(SecurityException()).`when`(securityService).verifyHasAnyRole(*READ_ROLES)

    // invoke and verify
    StepVerifier.create(accidentRegisterService.findChecked(1, 25, null, null))
      .expectError(PermissionDeniedException::class.java)
      .verify()
    //assertThrows(SecurityException::class.java, { .subscribe() })
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
}