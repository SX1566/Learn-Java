package cn.gftaxi.traffic.accident.dao.jpa.report

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.operation.dao.OperationDao
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentDaoImpl.getReportStatus].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class, OperationDao::class)
class GetReportStatusMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Get empty`() {
    StepVerifier.create(dao.getReportStatus(randomInt())).verifyComplete()
  }

  @Test
  fun `Get it`() {
    AuditStatus.values().forEach { getByReportStatus(it) }
  }

  fun getByReportStatus(status: AuditStatus) {
    // init data
    val (accidentCase, accidentSituation) = randomCase(
      id = null,
      happenTime = OffsetDateTime.now(),
      stage = CaseStage.Reporting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafted,
      registerStatus = AuditStatus.Approved,
      overdueRegister = false,
      reportStatus = status
    )
    caseRepository.save(accidentCase)
    Assertions.assertNotNull(accidentCase.id)
    accidentSituation.id = accidentCase.id
    situationRepository.saveAndFlush(accidentSituation)

    // invoke and verify
    StepVerifier.create(dao.getReportStatus(accidentCase.id!!))
      .expectNext(status).verifyComplete()
  }
}