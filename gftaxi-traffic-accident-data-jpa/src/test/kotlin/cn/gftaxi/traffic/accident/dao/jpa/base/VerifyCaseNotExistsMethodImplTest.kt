package cn.gftaxi.traffic.accident.dao.jpa.base

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.exception.NonUniqueException
import java.time.OffsetDateTime

/**
 * Test [AccidentDaoImpl.nextCaseCode].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class VerifyCaseNotExistsMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Verify success`() {
    StepVerifier.create(dao.verifyCaseNotExists(randomString(), OffsetDateTime.now()))
      .verifyComplete()
  }

  @Test
  fun `Verify failed`() {
    // init data
    val (accidentCase, _) = randomCase(
      id = null,
      happenTime = OffsetDateTime.now(),
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.saveAndFlush(accidentCase)
    Assertions.assertNotNull(accidentCase.id)

    // invoke and verify
    StepVerifier.create(dao.verifyCaseNotExists(accidentCase.carPlate!!, accidentCase.happenTime!!))
      .expectError(NonUniqueException::class.java)
      .verify()
  }
}