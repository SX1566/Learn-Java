package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentRegisterDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextId
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.random
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Update
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.lang.UnsupportedOperationException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * Test [AccidentRegisterDaoImpl.update].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class UpdateMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  private fun randomDto4Update(nullDescribe: Boolean = false): AccidentRegisterDto4Update {
    return AccidentRegisterDto4Update().apply {
      location = nextCode("location")
      carPlate = nextCode("car")
      driverName = nextCode("driver")
      hitForm = nextCode("hitForm")
      hitType = nextCode("hitType")
      describe = (if (nullDescribe) null else nextCode("describe")) // test for set null value
      happenTime = OffsetDateTime.now()
    }
  }

  private fun randomRegister(happenTime: OffsetDateTime, status: AccidentRegister.Status,
                             overdue: Boolean? = null, registerTime: OffsetDateTime? = null)
    : AccidentRegister {
    val accidentDraft = randomAccidentDraft(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = if (status == Status.Draft) Todo else AccidentDraft.Status.Done,
      happenTime = happenTime
    )
    em.persist(accidentDraft)
    val accidentRegister = POUtils.randomAccidentRegister(
      draft = accidentDraft,
      status = status,
      driverType = DriverType.Official,
      overdue = overdue,
      registerTime = registerTime
    )
    em.persist(accidentRegister)
    return accidentRegister
  }

  @Test
  fun `Case not exists`() {
    // init data
    val dto = randomDto4Update()

    // invoke and verify
    StepVerifier.create(dao.update(9999, dto.data)).expectNext(false).verifyComplete()
  }

  @Test
  fun `Update nothing`() {
    // init data
    val happenTime = OffsetDateTime.now()
    val po = randomAccidentDraft(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = Todo,
      happenTime = happenTime
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
  fun `Success update main properties`() {
    // 1. 构建一条事故登记数据
    val happenTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val po = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    em.flush();em.clear()

    // 2. 测试更新一个字符串属性的情况
    var dto = AccidentRegisterDto4Update().apply { location = random("location") }
    updateMainPropertiesAndVerify(po.id!!, dto)

    // 3. 测试更新一个日期属性的情况
    dto = AccidentRegisterDto4Update().apply { this.happenTime = happenTime.minusHours(1) }
    updateMainPropertiesAndVerify(po.id!!, dto)

    // 4. 测试全部属性都更新一遍
    val excludePropertyKeys = listOf("cars", "peoples", "others", "data")
    dto = AccidentRegisterDto4Update()
    dto::class.memberProperties.filter { !excludePropertyKeys.contains(it.name) }.forEach {
      it as KMutableProperty<*>
      when (it.returnType.classifier) {
        String::class -> it.setter.call(dto, random("Str"))
        Int::class -> it.setter.call(dto, nextId("Int"))
        Short::class -> it.setter.call(dto, nextId("Int").toShort())
        BigDecimal::class -> it.setter.call(dto, BigDecimal("${nextId("Int")}.0"))
        DriverType::class -> it.setter.call(dto, DriverType.Shift)
        LocalDate::class -> it.setter.call(dto, LocalDate.now())
        OffsetDateTime::class -> it.setter.call(dto, OffsetDateTime.now())
        else -> throw UnsupportedOperationException(it.returnType.toString())
      }
    }
    updateMainPropertiesAndVerify(po.id!!, dto)
  }

  private fun updateMainPropertiesAndVerify(id: Int, dto: AccidentRegisterDto4Update) {
    // invoke and verify
    StepVerifier.create(dao.update(id, dto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 验证 po 的相关属性应该更新了
    val updatedPo = em.find(AccidentRegister::class.java, id)
    assertNotNull(updatedPo)
    dto.data.forEach { key, value ->
      assertEquals(value, AccidentRegister::class.memberProperties.first { it.name == key }.get(updatedPo))
    }
  }
}