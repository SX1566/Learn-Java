package cn.gftaxi.traffic.accident.dao.jpa.report

import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier

/**
 * Test [AccidentDaoImpl.findReport].
 *
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
internal class FindMethodImplTest @Autowired constructor(
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

  @Test
  fun `Found something`() {
    // TODO
  }
}