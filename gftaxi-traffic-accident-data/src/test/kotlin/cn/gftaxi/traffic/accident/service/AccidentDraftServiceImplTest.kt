package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.verifyError
import tech.simter.security.SecurityService
import java.time.OffsetDateTime

/**
 * 测试事故报案 Service 实现。
 *
 * @author JF
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDraftDao::class, SecurityService::class)
class AccidentDraftServiceImplTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: SecurityService
) {
  @Test
  fun findWithRole() {
    // mock
    val now = OffsetDateTime.now()
    val pageNo = 1
    val pageSize = 25
    val po = AccidentDraft(null, "code", Status.Done, "plate", "driver", now, now, "", "", "", true, "", "", "", "")
    val expected = PageImpl(listOf(po), PageRequest.of(pageNo, pageSize), 1)
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    `when`(accidentDraftDao.find(pageNo, pageSize, Status.Todo, "search")).thenReturn(Mono.just(expected))

    // invoke
    val actual = accidentDraftService.find(pageNo, pageSize, Status.Todo, "search")

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    verify(accidentDraftDao).find(pageNo, pageSize, Status.Todo, "search")
  }

  @Test
  fun findWithoutRole() {
    // mock
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentDraftService.find(1, 25, Status.Todo, "").subscribe() })
  }

  @Test
  fun findTodo() {
    // mock
    val now = OffsetDateTime.now()
    val expected = AccidentDraft(null, "code", Status.Done, "plate", "driver", now, now, "", "", "", true, "", "", "", "")
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    `when`(accidentDraftDao.findTodo()).thenReturn(Flux.just(expected))

    // invoke
    val actual = accidentDraftService.findTodo()

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    verify(accidentDraftDao).findTodo()
  }

  @Test
  fun get() {
    // mock
    val code = "code"
    val now = OffsetDateTime.now()
    val expected = AccidentDraft(null, code, Status.Done, "plate", "driver", now, now, "", "", "", true, "", "", "", "")
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    `when`(accidentDraftDao.get(code)).thenReturn(Mono.just(expected))

    // invoke
    val actual = accidentDraftService.get(code)

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    verify(accidentDraftDao).get(code)
  }

  @Test
  fun submitWithRole() {
    // mock
    val expected = "code"
    val dto = AccidentDraftDto4Submit("plate", "driver", OffsetDateTime.now(), "location", "", "", "", "", "", "")
    val po = AccidentDraft(null,
      expected, Status.Todo, dto.carPlate, dto.driverName, dto.happenTime, dto.reportTime, dto.location, dto.hitForm,
      dto.hitType, false, dto.source, dto.authorName, dto.authorId, dto.describe
    )
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    `when`(accidentDraftDao.nextCode(dto.happenTime)).thenReturn(Mono.just(expected))
    `when`(accidentDraftDao.create(po)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    verify(accidentDraftDao).nextCode(dto.happenTime)
    verify(accidentDraftDao).create(po)
  }

  @Test
  fun submitWithoutRole() {
    // mock
    val dto = AccidentDraftDto4Submit("plate", "driver", OffsetDateTime.now(), "location", "", "", "", "", "", "")
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentDraftService.submit(dto).subscribe() })
  }

  @Test
  fun submitWithException() {
    // mock
    val expected = "code"
    val dto = AccidentDraftDto4Submit("plate", "driver", OffsetDateTime.now(), "location", "", "", "", "", "", "")
    val po = AccidentDraft(null,
      expected, Status.Todo, dto.carPlate, dto.driverName, dto.happenTime, dto.reportTime, dto.location, dto.hitForm,
      dto.hitType, false, dto.source, dto.authorName, dto.authorId, dto.describe
    )
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    `when`(accidentDraftDao.nextCode(dto.happenTime)).thenReturn(Mono.just(expected))
    doThrow(IllegalArgumentException()).`when`(accidentDraftDao).create(po)

    // invoke and verify
    StepVerifier.create(accidentDraftService.submit(dto)).verifyError(IllegalArgumentException::class)
  }

  @Test
  fun modifyWithRole() {
    // mock
    val code = "code"
    val dto = AccidentDraftDto4Modify("plate", "driver", OffsetDateTime.now(), "location", "hitForm", "hitType", "desc")
    val data = mapOf("carPlate" to dto.carPlate, "driverName" to dto.driverName, "happenTime" to dto.happenTime
      , "location" to dto.location, "hitForm" to dto.hitForm, "hitType" to dto.hitType, "describe" to dto.describe)
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.update(code, data)).thenReturn(Mono.just(true))

    // invoke
    val actual = accidentDraftService.modify(code, dto)

    // verify
    StepVerifier.create(actual).expectNext().verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).update(code, data)
  }

  @Test
  fun modifyWithoutRole() {
    // mock
    val code = "code"
    val dto = AccidentDraftDto4Modify("plate", "driver", OffsetDateTime.now(), "location", "hitForm", "hitType", "desc")
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentDraftService.modify(code, dto).subscribe() })
  }

  @Test
  fun modifyWithException() {
    // mock
    val code = "code"
    val dto = AccidentDraftDto4Modify("plate", "driver", OffsetDateTime.now(), "location", "hitForm", "hitType", "desc")
    val data = mapOf("carPlate" to dto.carPlate, "driverName" to dto.driverName, "happenTime" to dto.happenTime
      , "location" to dto.location, "hitForm" to dto.hitForm, "hitType" to dto.hitType, "describe" to dto.describe)
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.update(code, data)).thenReturn(Mono.just(false))

    // invoke
    val actual = accidentDraftService.modify(code, dto)

    // verify
    StepVerifier.create(actual).verifyError(IllegalArgumentException::class)
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).update(code, data)
  }
}