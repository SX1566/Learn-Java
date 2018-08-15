package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDraftDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentDraftDaoImpl.nextCode].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class NextCodeMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
  @Test
  fun withoutAnyData() {
    // init data
    val now = OffsetDateTime.now()
    val ymd = now.format(FORMAT_TO_YYYYMMDD)

    // invoke and verify
    StepVerifier.create(dao.nextCode(now)).expectNext("${ymd}_01").verifyComplete()
  }

  @Test
  fun withoutSameDayData() {
    // init data
    val today = OffsetDateTime.now()
    val yesterday = today.minusDays(1)
    val ymd4today = today.format(FORMAT_TO_YYYYMMDD)
    val ymd4yesterday = yesterday.format(FORMAT_TO_YYYYMMDD)
    em.run {
      persist(randomAccidentDraft(
        code = "${ymd4yesterday}_01",
        status = Todo,
        happenTime = yesterday
      ))
      flush()
      clear()
    }

    // invoke and verify
    StepVerifier.create(dao.nextCode(today)).expectNext("${ymd4today}_01").verifyComplete()
  }

  @Test
  fun withSameDayData() {
    // init data
    val now = OffsetDateTime.now().withHour(12)
    val ymd = now.format(FORMAT_TO_YYYYMMDD)
    em.run {
      persist(randomAccidentDraft(
        code = "${ymd}_02",
        status = Todo,
        happenTime = now.minusHours(1)
      ))
      persist(randomAccidentDraft(
        code = "${ymd}_01",
        status = Todo,
        happenTime = now.plusHours(1)
      ))
      flush()
      clear()
    }

    // invoke and verify
    StepVerifier.create(dao.nextCode(now)).expectNext("${ymd}_03").verifyComplete()
  }

  @Test
  fun withMultipleDayData() {
    // init data
    val today = OffsetDateTime.now()
    val yesterday = today.minusDays(1)
    val ymd4today = today.format(FORMAT_TO_YYYYMMDD)
    val ymd4yesterday = yesterday.format(FORMAT_TO_YYYYMMDD)
    em.run {
      persist(randomAccidentDraft(
        code = "${ymd4today}_01",
        status = Todo,
        happenTime = today
      ))
      persist(randomAccidentDraft(
        code = "${ymd4today}_02",
        status = Todo,
        happenTime = today
      ))
      persist(randomAccidentDraft(
        code = "${ymd4yesterday}_01",
        status = Todo,
        happenTime = yesterday
      ))
      flush()
      clear()
    }

    // invoke and verify
    StepVerifier.create(dao.nextCode(today)).expectNext("${ymd4today}_03").verifyComplete()
    StepVerifier.create(dao.nextCode(yesterday)).expectNext("${ymd4yesterday}_02").verifyComplete()
  }
}