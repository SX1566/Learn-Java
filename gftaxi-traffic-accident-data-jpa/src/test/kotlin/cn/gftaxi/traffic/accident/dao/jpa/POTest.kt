package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.common.AuditStatus.ToSubmit
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentCar
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentOther
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentPeople
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

private const val MODULE = "cn.gftaxi.traffic.accident"

/**
 * PO 测试。
 *
 * @author RJ
 */
@SpringJUnitConfig(POTest.Cfg::class)
@DataJpaTest
class POTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository
) {
  @Configuration
  @EnableJpaRepositories("$MODULE.dao.jpa.repository")
  @EntityScan("$MODULE.po", "$MODULE.dto")
  class Cfg

  @Test
  fun test() {
    // create
    val baseTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val (accidentCase, accidentSituation) = randomCase(
      id = null,
      happenTime = baseTime,
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = ToSubmit
    )
    accidentCase.apply {
      cars = listOf(randomAccidentCar(parent = this, sn = 0).apply {
        type = "自车"
        name = accidentCase.carPlate
      })
      peoples = (0..1).map {
        randomAccidentPeople(parent = this, sn = it.toShort()).apply {
          type = if (it == 0) "自车" else "三者"
        }
      }
      others = (0..1).map { randomAccidentOther(parent = this, sn = it.toShort()) }
    }

    // save
    caseRepository.save(accidentCase)
    assertNotNull(accidentCase.id)
    situationRepository.save(accidentSituation.apply { id = accidentCase.id })

    // verify
    assertEquals(1, caseRepository.count())
    assertEquals(1, situationRepository.count())
  }
}