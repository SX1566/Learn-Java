package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
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
import tech.simter.security.SecurityService

/**
 * Test [AccidentDraftServiceImpl.find].
 *
 * @author JF
 * @author RJ
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDraftDao::class, SecurityService::class, BcDao::class)
class FindMethodImplTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val accidentDraftDao: AccidentDraftDao,
  private val securityService: SecurityService
) {
  @Test
  fun findWithRole() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val po = randomAccidentDraft(status = Status.Done, overdueDraft = true)
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
    assertThrows(SecurityException::class.java) { accidentDraftService.find(1, 25, Status.Todo, "").subscribe() }
  }

  @Test
  fun findTodo() {
    // mock
    val expected = randomAccidentDraft(status = Status.Done, overdueDraft = true)
    doNothing().`when`(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    `when`(accidentDraftDao.findTodo()).thenReturn(Flux.just(expected))

    // invoke
    val actual = accidentDraftService.findTodo()

    // verify
    StepVerifier.create(actual).expectNext(expected).verifyComplete()
    verify(securityService).verifyHasRole(AccidentDraft.ROLE_READ)
    verify(accidentDraftDao).findTodo()
  }
}