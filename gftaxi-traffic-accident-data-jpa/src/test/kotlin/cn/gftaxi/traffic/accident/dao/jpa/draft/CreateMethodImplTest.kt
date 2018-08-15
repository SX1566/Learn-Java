package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDraftDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.exception.NonUniqueException
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentDraftDaoImpl.create].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class CreateMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
  @Test
  fun success() {
    // init data
    val po = randomAccidentDraft(
      code = "20180101_01",
      status = AccidentDraft.Status.Todo,
      happenTime = OffsetDateTime.now()
    )

    // invoke
    val actual = dao.create(po)
    em.flush()
    em.clear()

    // verify
    StepVerifier.create(actual).expectNext(po).verifyComplete()
    assertEquals(po,
      em.createQuery("select a from AccidentDraft a where id = :id", AccidentDraft::class.java)
        .setParameter("id", po.id)
        .singleResult
    )
  }

  @Test
  fun failedBySameCarAndHappenTime() {
    // init data
    val existsPo = randomAccidentDraft(
      code = "20180101_01",
      status = AccidentDraft.Status.Todo,
      happenTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    )
    em.persist(existsPo)
    em.flush()
    em.clear()

    // 创建车号与事发时间同上的 PO
    val newPo = randomAccidentDraft(
      code = "20180101_02",
      status = AccidentDraft.Status.Todo,
      happenTime = existsPo.happenTime,
      carPlate = existsPo.carPlate
    )

    // invoke
    val actual = dao.create(newPo)

    // verify
    StepVerifier.create(actual)
      .consumeErrorWith {
        assertTrue(it is NonUniqueException)
        assertEquals("指定车号和事发时间的案件已经存在！", it.message)
      }.verify()
    assertNull(newPo.id)
  }

  @Test
  fun failedBySameCode() {
    // init data
    val existsPo = randomAccidentDraft(
      code = "20180101_01",
      status = AccidentDraft.Status.Todo,
      happenTime = OffsetDateTime.now().minusDays(1)
    )
    em.persist(existsPo)
    em.flush()
    em.clear()

    // 创建事故编号同上的 PO
    val newPo = randomAccidentDraft(
      code = "20180101_01",
      status = AccidentDraft.Status.Todo,
      happenTime = OffsetDateTime.now()
    )

    // invoke
    val actual = dao.create(newPo)

    // verify
    StepVerifier.create(actual)
      .consumeErrorWith {
        assertTrue(it is NonUniqueException)
        assertEquals("相同编号的案件已经存在！", it.message)
      }.verify()
    assertNull(newPo.id)
  }
}