package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.POUtils.random
import cn.gftaxi.traffic.accident.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.verifyError
import tech.simter.exception.NonUniqueException
import tech.simter.security.SecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentDraftServiceImpl.submit].
 *
 * @author JF
 * @author RJ
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDraftDao::class, SecurityService::class, BcDao::class)
class SubmitMethodImplTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: SecurityService,
  private val bcDao: BcDao
) {
  @Test
  fun submitWithRole() {
    // mock
    val expected = Pair(1, "code")
    val po = randomAccidentDraft(id = expected.first, code = expected.second, status = Status.Done, overdueDraft = true)
    val dto = AccidentDraftDto4Submit().apply {
      carPlate = po.carPlate
      driverName = po.driverName
      happenTime = po.happenTime
      location = po.location
      hitForm = po.hitForm
      hitType = po.hitType
      source = "BC"
      authorName = random("authorName")
      authorId = random("authorId")
    }
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    `when`(accidentDraftDao.nextCode(dto.happenTime!!)).thenReturn(Mono.just(expected.second))
    `when`(accidentDraftDao.create(any())).thenReturn(Mono.just(po))
    `when`(bcDao.getMotorcadeName(any(), any())).thenReturn(Mono.just("test"))

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    verify(accidentDraftDao).nextCode(dto.happenTime!!)
    verify(accidentDraftDao).create(any())
    verify(bcDao).getMotorcadeName(any(), any())
  }

  @Test
  fun submitWithoutRole() {
    // mock
    val dto = AccidentDraftDto4Submit().apply {
      carPlate = "plate"
      driverName = "driver"
      happenTime = OffsetDateTime.now()
      location = "location"
      source = "BC"
      authorName = random("authorName")
      authorId = random("authorId")
    }
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)

    // invoke and verify
    assertThrows(SecurityException::class.java) { accidentDraftService.submit(dto).subscribe() }
  }

  @Test
  fun submitButNonUnique() {
    // mock
    val code = "code"
    val po = randomAccidentDraft(id = 1, code = code, status = Status.Done, overdueDraft = true)
    val dto = AccidentDraftDto4Submit().apply {
      carPlate = po.carPlate
      driverName = po.driverName
      happenTime = po.happenTime
      location = po.location
      hitForm = po.hitForm
      hitType = po.hitType
      source = "BC"
      authorName = random("authorName")
      authorId = random("authorId")
    }
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    `when`(accidentDraftDao.nextCode(dto.happenTime!!)).thenReturn(Mono.just(code))
    `when`(accidentDraftDao.create(any())).thenReturn(Mono.error(NonUniqueException()))
    `when`(bcDao.getMotorcadeName(any(), any())).thenReturn(Mono.just("test"))

    // invoke and verify
    StepVerifier.create(accidentDraftService.submit(dto)).verifyError(NonUniqueException::class)
    verify(accidentDraftDao).nextCode(dto.happenTime!!)
    verify(bcDao).getMotorcadeName(any(), any())
    verify(accidentDraftDao).create(any())
  }
}