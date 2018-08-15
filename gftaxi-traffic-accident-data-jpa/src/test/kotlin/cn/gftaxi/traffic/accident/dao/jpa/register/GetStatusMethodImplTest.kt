package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentRegisterDao.getStatus].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class GetStatusMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  @Test
  fun exists() {
    // init data
    val accidentDraft = randomAccidentDraft(
      code = "20180101_01",
      status = AccidentDraft.Status.Todo,
      happenTime = OffsetDateTime.now()
    )
    em.persist(accidentDraft)
    val accidentRegister = randomAccidentRegister(
      draft = accidentDraft,
      status = Approved,
      driverType = Official
    )
    em.persist(accidentRegister)
    em.flush()
    em.clear()

    // invoke
    val actual = dao.getStatus(accidentRegister.id!!)

    // verify
    StepVerifier.create(actual).expectNext(accidentRegister.status).verifyComplete()
  }

  @Test
  fun notExists() {
    // invoke
    val actual = dao.getStatus(9999)

    // verify
    StepVerifier.create(actual).verifyComplete()
  }
}