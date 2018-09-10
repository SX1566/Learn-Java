package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.POUtils.random
import cn.gftaxi.traffic.accident.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import com.nhaarman.mockito_kotlin.any
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
import tech.simter.exception.NonUniqueException
import tech.simter.exception.NotFoundException
import tech.simter.security.SecurityService
import java.time.OffsetDateTime

/**
 * 测试事故报案 Service 实现。
 *
 * @author JF
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDraftDao::class, SecurityService::class, BcDao::class)
class AccidentDraftServiceImplTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: SecurityService,
  private val bcDao: BcDao
) {
  @Test
  fun findWithRole() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val po = randomAccidentDraft(status = Status.Done, overdue = true)
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
    val expected = randomAccidentDraft(status = Status.Done, overdue = true)
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
    val id = 1
    val expected = randomAccidentDraft(status = Status.Done, overdue = true)
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.just(expected))

    // invoke
    val actual = accidentDraftService.get(id)

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    verify(accidentDraftDao).get(id)
  }

  @Test
  fun submitWithRole() {
    // mock
    val expected = Pair(1, "code")
    val po = randomAccidentDraft(id = expected.first, code = expected.second, status = Status.Done, overdue = true)
    val dto = AccidentDraftDto4Submit(
      carPlate = po.carPlate,
      driverName = po.driverName,
      happenTime = po.happenTime,
      location = po.location,
      hitForm = po.hitForm,
      hitType = po.hitType,
      source = "BC",
      authorName = random("authorName"),
      authorId = random("authorId")
    )
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    `when`(accidentDraftDao.nextCode(dto.happenTime)).thenReturn(Mono.just(expected.second))
    `when`(accidentDraftDao.create(any())).thenReturn(Mono.just(po))
    `when`(bcDao.getMotorcadeName(any(), any())).thenReturn(Mono.just("test"))

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    verify(accidentDraftDao).nextCode(dto.happenTime)
    verify(accidentDraftDao).create(any())
    verify(bcDao).getMotorcadeName(any(), any())
  }

  @Test
  fun submitWithoutRole() {
    // mock
    val dto = AccidentDraftDto4Submit(
      carPlate = "plate",
      driverName = "driver",
      happenTime = OffsetDateTime.now(),
      location = "location",
      source = "BC",
      authorName = random("authorName"),
      authorId = random("authorId")
    )
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentDraftService.submit(dto).subscribe() })
  }

  @Test
  fun submitButNonUnique() {
    // mock
    val code = "code"
    val po = randomAccidentDraft(id = 1, code = code, status = Status.Done, overdue = true)
    val dto = AccidentDraftDto4Submit(
      carPlate = po.carPlate,
      driverName = po.driverName,
      happenTime = po.happenTime,
      location = po.location,
      hitForm = po.hitForm,
      hitType = po.hitType,
      source = "BC",
      authorName = random("authorName"),
      authorId = random("authorId")
    )
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    `when`(accidentDraftDao.nextCode(dto.happenTime)).thenReturn(Mono.just(code))
    `when`(accidentDraftDao.create(any())).thenReturn(Mono.error(NonUniqueException()))
    `when`(bcDao.getMotorcadeName(any(), any())).thenReturn(Mono.just("test"))

    // invoke and verify
    StepVerifier.create(accidentDraftService.submit(dto)).verifyError(NonUniqueException::class)
    verify(accidentDraftDao).nextCode(dto.happenTime)
    verify(bcDao).getMotorcadeName(any(), any())
    verify(accidentDraftDao).create(any())
  }

  @Test
  fun modifyWithRole() {
    // mock
    val id = 1
    val data = mutableMapOf<String, Any?>().withDefault { null }
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.update(any(), any())).thenReturn(Mono.just(true))

    // invoke
    val actual = accidentDraftService.modify(id, data)

    // verify
    StepVerifier.create(actual).expectNext().verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).update(id, data)
  }

  @Test
  fun modifyWithRoleChangeCarPlateOrHappenTime() {
    // mock
    val id = 1
    val now = OffsetDateTime.now()
    val motorcadeName = "第一大队"
    val data = mutableMapOf<String, Any?>().withDefault { null }
    data.put("carPlate", "a")
    data.put("happenTime", now)
    val accidentDraft = AccidentDraft(id = 1, code = "code1", status = AccidentDraft.Status.Todo, carPlate = "plate001"
      , driverName = "driver001", happenTime = now, reportTime = now.minusHours(12), location = "广州"
      , overdue = true, source = "BC", authorName = "Admin", authorId = "021")
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.just(accidentDraft))
    `when`(bcDao.getMotorcadeName(any(), any())).thenReturn(Mono.just(motorcadeName))
    `when`(accidentDraftDao.update(any(), any())).thenReturn(Mono.just(true))

    // invoke
    val actual = accidentDraftService.modify(id, data)

    // verify
    StepVerifier.create(actual).expectNext().verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).get(id)
    verify(bcDao).getMotorcadeName(any(), any())
    verify(accidentDraftDao).update(any(), any())
  }

  @Test
  fun modifyWithoutRole() {
    // mock
    val id = 1
    val data: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)

    // invoke and verify
    assertThrows(SecurityException::class.java, { accidentDraftService.modify(id, data).subscribe() })
  }

  @Test
  fun modifyWithNotFoundByGet() {
    // mock
    val id = 1
    var data = mutableMapOf<String, Any?>().withDefault { null }
    data.put("carPlate", "粤A.12345")
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentDraftService.modify(id, data)

    // verify
    StepVerifier.create(actual).verifyError(NotFoundException::class)
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).get(id)
  }

  @Test
  fun modifyWithNotFoundByUpdate() {
    // mock
    val id = 1
    var data = mutableMapOf<String, Any?>().withDefault { null }
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.empty())
    `when`(accidentDraftDao.update(any(), any())).thenReturn(Mono.just(false))

    // invoke
    val actual = accidentDraftService.modify(id, data)

    // verify
    StepVerifier.create(actual).verifyError(NotFoundException::class)
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).update(id, data)
  }
}