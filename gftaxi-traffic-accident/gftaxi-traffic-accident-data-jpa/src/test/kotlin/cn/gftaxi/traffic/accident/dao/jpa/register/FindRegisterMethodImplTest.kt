package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.AuditStatus.Rejected
import cn.gftaxi.traffic.accident.common.AuditStatus.ToSubmit
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4View
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime

/**
 * Test [AccidentDaoImpl.findRegister].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
class FindRegisterMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentDao
) {
  @Test
  fun `Found nothing`() {
    StepVerifier.create(dao.findRegister())
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(0, page.totalElements)
        assertTrue(page.content.isEmpty())
      }.verifyComplete()
  }

  /**
   * 不同登记状态 [AuditStatus] 的案件各创建一条，其报案时间按顺序严格递减
   */
  private fun initData(): List<Pair<AccidentCase, AccidentSituation>> {
    return AuditStatus.values().mapIndexed { index, stetus ->
      randomCase(id = null, registerStatus = stetus, happenTime = OffsetDateTime.now().minusHours(index.toLong()))
        .also {
          caseRepository.saveAndFlush(it.first)
          situationRepository.saveAndFlush(it.second.apply { id = it.first.id })
        }
    }
  }

  private fun verifyDto(accident: Pair<AccidentCase, AccidentSituation>, dto: AccidentRegisterDto4View) {
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
    assertEquals(accidentCase.hitType, dto.hitType)
    assertEquals(accidentCase.location, dto.location)
    // 验证处理信息
    assertEquals(accidentSituation.registerStatus, dto.registerStatus)
    assertEquals(accidentSituation.authorName, dto.authorName)
    assertEquals(accidentSituation.draftTime, dto.draftTime)
    assertEquals(accidentSituation.overdueDraft, dto.overdueDraft)
    assertEquals(accidentSituation.registerTime, dto.registerTime)
    assertEquals(accidentSituation.overdueRegister, dto.overdueRegister)
    assertEquals(accidentSituation.registerCheckedCount, dto.checkedCount)
    assertEquals(accidentSituation.registerCheckedComment, dto.checkedComment)
    assertEquals(accidentSituation.registerCheckedAttachments, dto.checkedAttachments)
  }

  @Test
  fun `Found something`() {
    val accidents = initData()

    // 不传状态，查出所有登记状态的案件
    StepVerifier.create(dao.findRegister())
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(4, page.totalElements)
        page.content.forEachIndexed { index, dto ->
          verifyDto(accidents[index], dto)
        }
      }.verifyComplete()

    // 传0个状态，查出所有登记状态的案件
    StepVerifier.create(dao.findRegister(registerStatuses = listOf()))
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
      StepVerifier.create(dao.findRegister(registerStatuses = listOf(stetus)))
        .consumeNextWith { page ->
          assertEquals(0, page.number)
          assertEquals(25, page.size)
          assertEquals(1, page.totalElements)
          verifyDto(accidents[index], page.content[0])
        }.verifyComplete()
    }

    // 传多个状态，查出所有登记状态的案件
    StepVerifier.create(dao.findRegister(registerStatuses = listOf(ToSubmit, Rejected)))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(2, page.totalElements)
        verifyDto(accidents[0], page.content[0])
        verifyDto(accidents[2], page.content[1])
      }.verifyComplete()
  }
}