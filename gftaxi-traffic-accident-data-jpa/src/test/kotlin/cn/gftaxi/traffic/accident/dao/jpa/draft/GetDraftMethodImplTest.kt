package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime

/**
 * Test [AccidentDaoImpl.getDraft].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
@Disabled
class GetDraftMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Try Single Column`() {
    // empty
    assertThrows<EmptyResultDataAccessException> { caseRepository.findCodeById(randomInt()) }
    assertFalse(caseRepository.findCodeById2(randomInt()).isPresent)
    assertTrue(caseRepository.findTopCode("code").isEmpty())

    // init data
    val (case, _) = randomCase(
      id = null,
      code = "code1",
      happenTime = OffsetDateTime.now(),
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(case)
    assertEquals(case.code, caseRepository.findCodeById(case.id!!))
    assertEquals(case.code, caseRepository.findCodeById2(case.id!!).get())

    val (case2, _) = randomCase(
      id = null,
      code = "code2",
      happenTime = OffsetDateTime.now(),
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(case2)
    val c = caseRepository.findTopCode("code")
    val f = c.first()
    println(f)
    assertEquals(1, c.size)
    assertEquals(case2.code, caseRepository.findTopCode("code").first().code)
  }

  @Test
  fun `Get empty`() {
    StepVerifier.create(dao.getDraft(randomInt())).verifyComplete()
  }

  @Test
  fun `Get it`() {
    // init data
    val pair = randomCase(
      id = null,
      happenTime = OffsetDateTime.now(),
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(pair.first)
    Assertions.assertNotNull(pair.first.id)
    pair.second.id = pair.first.id
    situationRepository.saveAndFlush(pair.second)
    val dto = AccidentDraftDto4Form.from(pair)

    // invoke and verify
    StepVerifier.create(dao.getDraft(pair.first.id!!))
      .expectNext(dto)
      .verifyComplete()
  }
}