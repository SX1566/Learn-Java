package cn.gftaxi.traffic.accident.dao.jpa.base

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime

/**
 * Test [AccidentDaoImpl.nextCaseCode].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class NextCaseCodeMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Without any data`() {
    // init data
    val now = OffsetDateTime.now()
    val ymd = now.format(FORMAT_TO_YYYYMMDD)

    // invoke and verify
    StepVerifier.create(dao.nextCaseCode(now)).expectNext("${ymd}_01").verifyComplete()
  }

  @Test
  fun `Without same day data`() {
    // init data
    val today = OffsetDateTime.now()
    val yesterday = today.minusDays(1)
    val ymd4today = today.format(FORMAT_TO_YYYYMMDD)
    val (accidentCase, _) = randomCase(
      id = null,
      happenTime = yesterday,
      code = yesterday.format(FORMAT_TO_YYYYMMDD) + "_01",
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(accidentCase)
    Assertions.assertNotNull(accidentCase.id)

    // invoke and verify
    StepVerifier.create(dao.nextCaseCode(today)).expectNext("${ymd4today}_01").verifyComplete()
  }

  @Test
  fun `With same day data`() {
    // init data
    val today = OffsetDateTime.now()
    val ymd4today = today.format(FORMAT_TO_YYYYMMDD)
    val (accidentCase, _) = randomCase(
      id = null,
      happenTime = today,
      code = today.format(FORMAT_TO_YYYYMMDD) + "_01",
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(accidentCase)
    Assertions.assertNotNull(accidentCase.id)

    // invoke and verify
    StepVerifier.create(dao.nextCaseCode(today)).expectNext("${ymd4today}_02").verifyComplete()
  }

  @Test
  fun `With multiple day data`() {
    // init data
    val today = OffsetDateTime.now().minusDays(10)
    val yesterday = today.minusDays(1)
    val ymd4today = today.format(FORMAT_TO_YYYYMMDD)
    val ymd4yesterday = yesterday.format(FORMAT_TO_YYYYMMDD)

    val (accidentCase1, _) = randomCase(
      id = null,
      happenTime = today,
      code = today.format(FORMAT_TO_YYYYMMDD) + "_01",
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(accidentCase1)
    Assertions.assertNotNull(accidentCase1.id)

    val (accidentCase2, _) = randomCase(
      id = null,
      happenTime = today,
      code = today.format(FORMAT_TO_YYYYMMDD) + "_02",
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(accidentCase2)
    Assertions.assertNotNull(accidentCase2.id)

    val (accidentCase3, _) = randomCase(
      id = null,
      happenTime = yesterday,
      code = yesterday.format(FORMAT_TO_YYYYMMDD) + "_01",
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(accidentCase3)
    Assertions.assertNotNull(accidentCase3.id)

    // invoke and verify
    StepVerifier.create(dao.nextCaseCode(today)).expectNext("${ymd4today}_03").verifyComplete()
    StepVerifier.create(dao.nextCaseCode(yesterday)).expectNext("${ymd4yesterday}_02").verifyComplete()
  }
}