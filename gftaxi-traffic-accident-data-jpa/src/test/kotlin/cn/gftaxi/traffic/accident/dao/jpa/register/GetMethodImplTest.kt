package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentRegisterDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.util.ClassTypeInformation
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentRegisterDaoImpl.get].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class GetMethodImplTest @Autowired constructor(
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
      status = Draft,
      driverType = Official
    )
    em.persist(accidentRegister)
    em.flush()
    em.clear()

    // invoke
    val actual = dao.get(accidentRegister.id!!)

    // verify
    StepVerifier.create(actual)
      .consumeNextWith {
        assertEquals(accidentRegister.id, it.id)
        assertEquals(accidentRegister.happenTime, it.happenTime)
        assertEquals(accidentRegister.carPlate, it.carPlate)
        assertEquals(accidentRegister.driverName, it.driverName)
        assertEquals(accidentRegister.driverType, it.driverType)
        assertTrue(it.cars!!.isEmpty())
        assertTrue(it.peoples!!.isEmpty())
        assertTrue(it.others!!.isEmpty())
      }.verifyComplete()
  }

  @Test
  fun notExists() {
    // invoke
    val actual = dao.get(9999)

    // verify
    StepVerifier.create(actual).verifyComplete()
  }
}