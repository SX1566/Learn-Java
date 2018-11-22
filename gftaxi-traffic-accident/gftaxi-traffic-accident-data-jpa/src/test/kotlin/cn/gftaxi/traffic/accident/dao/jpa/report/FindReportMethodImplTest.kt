package cn.gftaxi.traffic.accident.dao.jpa.report

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.AuditStatus.*
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentPeople
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import kotlin.test.assertEquals

/**
 * Test [AccidentDaoImpl.findReport].
 *
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
internal class FindReportMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Found nothing`() {
    StepVerifier.create(dao.findReport())
      .consumeNextWith { page ->
        Assertions.assertEquals(0, page.number)
        Assertions.assertEquals(25, page.size)
        Assertions.assertEquals(0, page.totalElements)
        Assertions.assertTrue(page.content.isEmpty())
      }.verifyComplete()
  }

  /**
   * 创建案件
   * 不同报告状态 [AuditStatus] 各一条，不属于报告阶段的案件一条
   * 其报案时间按创建顺序严格递减
   */
  private fun initData(): List<Pair<AccidentCase, AccidentSituation>> {
    val values = AuditStatus.values()
    val size = values.size
    return List(size + 1) {
      randomCase(
        id = null,
        reportStatus = if (it < size) values[it] else null,
        happenTime = OffsetDateTime.now().minusHours(it.toLong()),
        registerStatus = if (it < size) Approved else Rejected
      )
        .also {
          caseRepository.saveAndFlush(it.first.apply {
            peoples = listOf(randomAccidentPeople(parent = it.first, type = "自车"))
          })
          situationRepository.saveAndFlush(it.second.apply { id = it.first.id })
        }
    }
  }

  private fun verifyDto(accident: Pair<AccidentCase, AccidentSituation>, dto: AccidentReportDto4View) {
    val accidentCase = accident.first
    val accidentSituation = accident.second
    // 验证基本信息
    assertEquals(accidentCase.id, dto.id)
    assertEquals(accidentCase.code, dto.code)
    assertEquals(accidentCase.motorcadeName, dto.motorcadeName)
    assertEquals(accidentCase.carPlate, dto.carPlate)
    assertEquals(accidentCase.driverName, dto.driverName)
    assertEquals(accidentCase.driverType, dto.driverType)
    assertEquals(accidentCase.happenTime, dto.happenTime)
    assertEquals(accidentCase.hitForm, dto.hitForm)
    assertEquals(accidentCase.location, dto.location)
    assertEquals(accidentCase.carModel, dto.carModel)
    assertEquals(accidentCase.level, dto.level)
    assertEquals(accidentCase.appointDriverReturnTime, dto.appointDriverReturnTime)
    assertEquals(accidentCase.peoples!![0].duty, dto.duty)
    // 验证处理信息
    assertEquals(accidentSituation.reportStatus, dto.reportStatus)
    assertEquals(accidentSituation.draftTime, dto.draftTime)
    assertEquals(accidentSituation.overdueDraft, dto.overdueDraft)
    assertEquals(accidentSituation.registerTime, dto.registerTime)
    assertEquals(accidentSituation.overdueRegister, dto.overdueRegister)
    assertEquals(accidentSituation.reportTime, dto.reportTime)
    assertEquals(accidentSituation.overdueReport, dto.overdueReport)
    assertEquals(accidentSituation.reportCheckedCount, dto.checkedCount)
    assertEquals(accidentSituation.reportCheckedComment, dto.checkedComment)
    assertEquals(accidentSituation.reportCheckedAttachments, dto.checkedAttachments)
  }

  @Test
  fun `Found something`() {
    val accidents = initData()

    // 不传状态，查出所有报告状态的案件
    StepVerifier.create(dao.findReport())
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(4, page.totalElements)
        page.content.forEachIndexed { index, dto ->
          verifyDto(accidents[index], dto)
        }
      }.verifyComplete()

    // 传0个状态，查出所有报告状态的案件
    StepVerifier.create(dao.findReport(reportStatuses = listOf()))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(4, page.totalElements)
        page.content.forEachIndexed { index, dto ->
          verifyDto(accidents[index], dto)
        }
      }.verifyComplete()

    // 传1个状态，验证各种单个状态
    AuditStatus.values().mapIndexed { index, stetus ->
      StepVerifier.create(dao.findReport(reportStatuses = listOf(stetus)))
        .consumeNextWith { page ->
          assertEquals(0, page.number)
          assertEquals(25, page.size)
          assertEquals(1, page.totalElements)
          verifyDto(accidents[index], page.content[0])
        }.verifyComplete()
    }

    // 传多个状态，查出所有报告状态的案件
    StepVerifier.create(dao.findReport(reportStatuses = listOf(ToSubmit, Rejected)))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(2, page.totalElements)
        verifyDto(accidents[0], page.content[0])
        verifyDto(accidents[2], page.content[1])
      }.verifyComplete()
  }
}