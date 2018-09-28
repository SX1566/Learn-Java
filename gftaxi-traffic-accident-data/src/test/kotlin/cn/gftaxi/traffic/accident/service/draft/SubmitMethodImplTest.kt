package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_SUBMIT
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.exception.NonUniqueException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentDraftServiceImpl.submit].
 *
 * @author RJ
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class)
class SubmitMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentDraftService: AccidentDraftService
) {
  @Test
  fun `Success submit`() {
    // mock
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT)).thenReturn(Mono.empty())
    val pair = randomCase()
    val dto = AccidentDraftDto4Form.from(pair)
    `when`(accidentDao.verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)).thenReturn(Mono.empty())
    `when`(accidentDao.createCase(dto)).thenReturn(pair.toMono())

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual).expectNext(pair).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
    verify(accidentDao).verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)
    verify(accidentDao).createCase(dto)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT)).thenReturn(Mono.error(PermissionDeniedException()))
    val dto = AccidentDraftDto4Form().apply {
      carPlate = randomString("粤A.")
      happenTime = OffsetDateTime.now()
    }

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
    verify(accidentDao, times(0)).verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)
    verify(accidentDao, times(0)).createCase(dto)
  }

  @Test
  fun `Failed by NonUnique`() {
    // mock
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT)).thenReturn(Mono.empty())
    val dto = AccidentDraftDto4Form().apply {
      carPlate = randomString("粤A.")
      happenTime = OffsetDateTime.now()
    }
    `when`(accidentDao.verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!))
      .thenReturn(Mono.error(NonUniqueException()))

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual)
      .expectError(NonUniqueException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
    verify(accidentDao).verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)
    verify(accidentDao, times(0)).createCase(dto)
  }
}