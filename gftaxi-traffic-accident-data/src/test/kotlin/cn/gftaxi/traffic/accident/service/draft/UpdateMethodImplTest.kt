package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.po.AccidentDraft
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
import tech.simter.exception.NotFoundException
import tech.simter.security.SecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentDraftServiceImpl.update].
 *
 * @author JF
 * @author RJ
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDraftDao::class, SecurityService::class, BcDao::class)
class UpdateMethodImplTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: SecurityService,
  private val bcDao: BcDao
) {
  @Test
  fun updateWithRole() {
    // mock
    val id = 1
    val data = mutableMapOf<String, Any?>().withDefault { null }
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.update(any(), any())).thenReturn(Mono.just(true))

    // invoke
    val actual = accidentDraftService.update(id, data)

    // verify
    StepVerifier.create(actual).expectNext().verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).update(id, data)
  }

  @Test
  fun updateWithRoleChangeCarPlateOrHappenTime() {
    // mock
    val id = 1
    val now = OffsetDateTime.now()
    val motorcadeName = "第一大队"
    val data = mutableMapOf<String, Any?>().withDefault { null }
    data["carPlate"] = "a"
    data["happenTime"] = now
    val accidentDraft = AccidentDraft(id = 1, code = "code1", status = AccidentDraft.Status.Todo, carPlate = "plate001"
      , driverName = "driver001", happenTime = now, draftTime = now.minusHours(12), location = "广州"
      , overdueDraft = true, source = "BC", authorName = "Admin", authorId = "021")
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.just(accidentDraft))
    `when`(bcDao.getMotorcadeName(any(), any())).thenReturn(Mono.just(motorcadeName))
    `when`(accidentDraftDao.update(any(), any())).thenReturn(Mono.just(true))

    // invoke
    val actual = accidentDraftService.update(id, data)

    // verify
    StepVerifier.create(actual).expectNext().verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).get(id)
    verify(bcDao).getMotorcadeName(any(), any())
    verify(accidentDraftDao).update(any(), any())
  }

  @Test
  fun updateWithoutRole() {
    // mock
    val id = 1
    val data: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
    doThrow(SecurityException()).`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)

    // invoke and verify
    assertThrows(SecurityException::class.java) { accidentDraftService.update(id, data).subscribe() }
  }

  @Test
  fun updateWithNotFoundByGet() {
    // mock
    val id = 1
    val data = mutableMapOf<String, Any?>().withDefault { null }
    data["carPlate"] = "粤A.12345"
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentDraftService.update(id, data)

    // verify
    StepVerifier.create(actual).verifyError(NotFoundException::class)
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).get(id)
  }

  @Test
  fun updateWithNotFoundByUpdate() {
    // mock
    val id = 1
    val data = mutableMapOf<String, Any?>().withDefault { null }
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.empty())
    `when`(accidentDraftDao.update(any(), any())).thenReturn(Mono.just(false))

    // invoke
    val actual = accidentDraftService.update(id, data)

    // verify
    StepVerifier.create(actual).verifyError(NotFoundException::class)
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_MODIFY)
    verify(accidentDraftDao).update(id, data)
  }
}