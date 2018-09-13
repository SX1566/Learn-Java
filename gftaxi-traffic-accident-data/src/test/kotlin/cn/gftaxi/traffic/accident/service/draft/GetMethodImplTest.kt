package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.security.SecurityService

/**
 * Test [AccidentDraftServiceImpl.get].
 *
 * @author JF
 * @author RJ
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDraftDao::class, SecurityService::class, BcDao::class)
class GetMethodImplTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: SecurityService
) {
  @Test
  fun get() {
    // mock
    val id = 1
    val expected = randomAccidentDraft(status = Status.Done, overdueDraft = true)
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    `when`(accidentDraftDao.get(id)).thenReturn(Mono.just(expected))

    // invoke
    val actual = accidentDraftService.get(id)

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    verify(accidentDraftDao).get(id)
  }
}