package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDraftDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.reflect.full.memberProperties

/**
 * Test [AccidentDraftDaoImpl.update].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class UpdateMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
  private fun randomModifyDto(nullDescribe: Boolean = false): AccidentDraftDto4Modify {
    return AccidentDraftDto4Modify().apply {
      location = nextCode("location")
      carPlate = nextCode("carPlate")
      driverName = nextCode("driver")
      hitForm = nextCode("hitForm")
      hitType = nextCode("hitType")
      describe = if (nullDescribe) null else nextCode("describe")
      happenTime = OffsetDateTime.now()
    }
  }

  @Test
  fun notExists() {
    // init data
    val dto = randomModifyDto()

    // invoke and verify
    StepVerifier.create(dao.update(1, dto.data)).expectNext(false).verifyComplete()
  }

  @Test
  fun updateNothing() {
    // init data
    val now = OffsetDateTime.now().withHour(12)
    val ymd = now.format(FORMAT_TO_YYYYMMDD)
    val po = randomAccidentDraft(
      code = "${ymd}_01",
      status = Todo,
      happenTime = now.minusHours(1)
    )
    em.run {
      persist(po)
      flush()
      clear()
    }

    // invoke and verify
    StepVerifier.create(dao.update(po.id!!, mapOf())).expectNext(false).verifyComplete()
  }

  @Test
  fun successUpdateLocation() {
    // init data
    val now = OffsetDateTime.now().withHour(12)
    val ymd = now.format(FORMAT_TO_YYYYMMDD)
    val po1 = randomAccidentDraft(
      code = "${ymd}_02",
      status = Todo,
      happenTime = now.minusHours(1)
    )
    val po1OldLocation = po1.location
    val po2 = randomAccidentDraft(
      code = "${ymd}_01",
      status = Todo,
      happenTime = now.plusHours(1)
    )
    val po2OldLocation = po2.location
    em.run {
      persist(po1)
      persist(po2)
      flush()
      clear()
    }
    val newLocation = nextCode("location")

    // invoke and verify
    StepVerifier.create(dao.update(po1.id!!, mapOf("location" to newLocation)))
      .expectNext(true).verifyComplete()

    // po1 的 location 应该更新了
    val ql = "select location from AccidentDraft where id = :id"
    var location = em.createQuery(ql, String::class.java)
      .setParameter("id", po1.id)
      .singleResult
    assertNotEquals(po1OldLocation, location)
    assertEquals(newLocation, location)

    // po2 的 location 应该没更新
    location = em.createQuery(ql, String::class.java)
      .setParameter("id", po2.id)
      .singleResult
    assertEquals(po2OldLocation, location)
  }

  @Test
  fun successUpdateMultipleProperties() {
    // init data
    val now = OffsetDateTime.now().withHour(12)
    val ymd = now.format(FORMAT_TO_YYYYMMDD)
    val po = randomAccidentDraft(
      code = "${ymd}_01",
      status = Todo,
      happenTime = now.minusHours(1)
    )
    em.run {
      persist(po)
      flush()
      clear()
    }
    val modifyDto = randomModifyDto(nullDescribe = true)

    // invoke and verify
    StepVerifier.create(dao.update(po.id!!, modifyDto.data))
      .expectNext(true).verifyComplete()
    em.run {
      flush()
      clear()
    }

    // verify: po 的相关属性应该更新了
    val updatedPo = em.find(AccidentDraft::class.java, po.id)
    assertNotNull(updatedPo)
    modifyDto.data.forEach { key, value ->
      assertEquals(value, AccidentDraft::class.memberProperties.first { it.name == key }.get(updatedPo))
    }
  }
}