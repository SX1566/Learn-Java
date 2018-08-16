package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentRegisterDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Done
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentRegisterDaoImpl.checked].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class CheckedMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  private fun randomRegister(status: Status)
    : AccidentRegister {
    val happenTime = OffsetDateTime.now()
    val accidentDraft = randomAccidentDraft(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = if (status == Draft) Todo else Done,
      happenTime = happenTime
    )
    em.persist(accidentDraft)
    val accidentRegister = randomAccidentRegister(
      draft = accidentDraft,
      status = status,
      driverType = Official
    )
    em.persist(accidentRegister)
    return accidentRegister
  }

  @Test
  fun successChecked() {
    successChecked(passed = true)
    successChecked(passed = false)
  }

  private fun successChecked(passed: Boolean) {
    // init data
    val po = randomRegister(ToCheck)
    em.flush()
    em.clear()

    // invoke
    val actual = dao.checked(po.id!!, passed)

    // verify
    StepVerifier.create(actual).expectNext(true).verifyComplete()
    em.flush()
    em.clear()

    // 验证状态应已更新
    val updatedPo = em.find(AccidentRegister::class.java, po.id)
    assertNotEquals(po.status, updatedPo.status)
    assertEquals(if (passed) Approved else Rejected, updatedPo.status)
  }

  @Test
  fun failedWithIllegalStatus() {
    Status.values().filter { it != ToCheck }.forEach {
      failedWithIllegalStatus(status = it, passed = true)
      failedWithIllegalStatus(status = it, passed = false)
    }
  }

  private fun failedWithIllegalStatus(status: Status, passed: Boolean) {
    // init data
    val po = randomRegister(status)
    em.flush()
    em.clear()

    // invoke
    val actual = dao.checked(po.id!!, passed)

    // verify
    StepVerifier.create(actual).expectNext(false).verifyComplete()
    em.flush()
    em.clear()

    // 验证状态应没有更新
    val updatedPo = em.find(AccidentRegister::class.java, po.id)
    assertEquals(po.status, updatedPo.status)
  }

  @Test
  fun notExists() {
    StepVerifier.create(dao.checked(id = 9999, passed = true)).expectNext(false).verifyComplete()
    StepVerifier.create(dao.checked(id = 9999, passed = false)).expectNext(false).verifyComplete()
  }
}