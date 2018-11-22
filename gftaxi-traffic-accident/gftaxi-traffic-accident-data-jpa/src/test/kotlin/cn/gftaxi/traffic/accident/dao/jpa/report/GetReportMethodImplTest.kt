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
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
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
 * Test [AccidentDaoImpl.getReport].
 *
 * @suthor zh
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class, OperationDao::class)
class GetReportMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Get empty`() {
    StepVerifier.create(dao.getReport(randomInt())).verifyComplete()
  }

  @Test
  fun `Get it`() {
    // init data
    val pair = randomCase(
      id = null,
      happenTime = OffsetDateTime.now(),
      stage = CaseStage.Reporting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      overdueRegister = false,
      registerStatus = AuditStatus.Approved,
      overdueReport = false,
      reportStatus = AuditStatus.Approved
    )
    caseRepository.save(pair.first)
    Assertions.assertNotNull(pair.first.id)
    pair.second.id = pair.first.id
    situationRepository.saveAndFlush(pair.second)
    val dto = AccidentReportDto4Form.from(pair)

    // invoke and verify
    StepVerifier.create(dao.getReport(pair.first.id!!))
      .expectNext(dto)
      .verifyComplete()
  }
}