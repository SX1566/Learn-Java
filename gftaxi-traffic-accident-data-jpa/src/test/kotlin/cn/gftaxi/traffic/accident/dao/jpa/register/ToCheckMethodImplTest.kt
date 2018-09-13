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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.test.assertFalse

/**
 * Test [AccidentRegisterDaoImpl.toCheck].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class ToCheckMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  private fun randomRegister(happenTime: OffsetDateTime, status: Status,
                             overdue: Boolean? = null, registerTime: OffsetDateTime? = null)
    : AccidentRegister {
    val accidentDraft = randomAccidentDraft(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = if (status == Draft) Todo else Done,
      happenTime = happenTime
    )
    em.persist(accidentDraft)
    val accidentRegister = randomAccidentRegister(
      draft = accidentDraft,
      status = status,
      driverType = Official,
      overdue = overdue,
      registerTime = registerTime
    )
    em.persist(accidentRegister)
    return accidentRegister
  }

  @Test
  fun successWithAllowStatus() {
    successWithAllowStatus(status = Draft, fireSubmit = false)
    successWithAllowStatus(status = Rejected, fireSubmit = false)
  }

  private fun successWithAllowStatus(status: Status, fireSubmit: Boolean) {
    // init data
    val happenTime = OffsetDateTime.now().minusHours(10)
    val po = randomRegister(
      happenTime = happenTime,
      status = status,
      overdue = if (fireSubmit) null else false,
      registerTime = if (fireSubmit) null else happenTime.plusMinutes(1)
    )
    em.flush()
    em.clear()

    // invoke
    val now = OffsetDateTime.now()
    val actual = dao.toCheck(po.id!!)

    // verify
    StepVerifier.create(actual).expectNext(true).verifyComplete()
    em.flush()
    em.clear()

    val updatedPo = em.find(AccidentRegister::class.java, po.id)
    // 验证状态已更新
    assertNotEquals(po.status, updatedPo.status)
    assertEquals(ToCheck, updatedPo.status)

    // 验证 registerTime、overdueRegister 的更新情况
    if (!fireSubmit) {  // 非首次提交: 应只更新了 status 没有更新 registerTime、overdueRegister
      assertEquals(po.overdueRegister, updatedPo.overdueRegister)
      assertEquals(po.registerTime, updatedPo.registerTime)
    } else {            // 首次提交: 应同时更新了 status、registerTime、overdueRegister
      assertFalse(updatedPo.overdueRegister!!)
      assertTrue(updatedPo.registerTime!!.isAfter(now))
    }
  }

  @Test
  fun failedWithIllegalStatus() {
    failedWithIllegalStatus(Approved)
    failedWithIllegalStatus(ToCheck)
  }

  private fun failedWithIllegalStatus(status: Status) {
    // init data
    val happenTime = OffsetDateTime.now().minusHours(10)
    val po = randomRegister(
      happenTime = happenTime,
      status = status,
      overdue = false,
      registerTime = happenTime.plusMinutes(1)
    )
    em.flush()
    em.clear()

    // invoke
    val actual = dao.toCheck(po.id!!)

    // verify
    StepVerifier.create(actual).expectNext(false).verifyComplete()

    // po 应该没有任何更新
    val updatedPo = em.find(AccidentRegister::class.java, po.id)
    assertEquals(po.id, updatedPo.id)
    assertEquals(po.status, updatedPo.status)
    assertEquals(po.registerTime, updatedPo.registerTime)
    assertEquals(po.overdueRegister, updatedPo.overdueRegister)
  }

  @Test
  fun notExists() {
    // invoke
    val actual = dao.toCheck(9999)

    // verify
    StepVerifier.create(actual).expectNext(false).verifyComplete()
  }
}