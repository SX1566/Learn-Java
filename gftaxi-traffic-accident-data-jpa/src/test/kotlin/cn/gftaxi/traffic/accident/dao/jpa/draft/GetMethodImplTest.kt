package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDraftDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentDraftDaoImpl.get].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class GetMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
  @Test
  fun exists() {
    // init data
    val po = randomAccidentDraft(
      code = "20180101_01",
      status = AccidentDraft.Status.Todo,
      happenTime = OffsetDateTime.now()
    )
    em.persist(po)
    em.flush()
    em.clear()

    // invoke
    val actual = dao.get(po.id!!)

    // verify
    StepVerifier.create(actual).expectNext(po).verifyComplete()
  }

  @Test
  fun notExists() {
    // invoke
    val actual = dao.get(9999)

    // verify
    StepVerifier.create(actual).verifyComplete()
  }
}