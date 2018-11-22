package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.bc.dao.BcDao
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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentDaoImpl.getDraft].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
class GetDraftMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentDao
) {
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