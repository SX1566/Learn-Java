package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentRegisterDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextId
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.random
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentCar
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentOther
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentPeople
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomInt
import cn.gftaxi.traffic.accident.dto.AccidentCarDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentOtherDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentPeopleDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Update
import cn.gftaxi.traffic.accident.po.*
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentPeople.Sex.Female
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import org.junit.jupiter.api.Assertions.*
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

  private fun randomRegister(
    happenTime: OffsetDateTime,
    status: AccidentRegister.Status,
    overdue: Boolean? = null,
    registerTime: OffsetDateTime? = null,
    cars: Set<AccidentCar>? = null,
    peoples: Set<AccidentPeople>? = null,
    others: Set<AccidentOther>? = null
  )
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
      registerTime = registerTime,
      cars = cars,
      peoples = peoples,
      others = others
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
  fun `Main properties success update`() {
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
      when (it.returnType.classifier) { // 随机生成一些属性值
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

  @Test
  fun `Cars success create`() {
    // 1. 构建一条事故登记数据
    val happenTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    em.flush();em.clear()

    // 2. 添加当事车辆
    // 2.1 构建数据
    val carDto = AccidentCarDto4Update().apply {
      sn = 0
      name = registerPo.carPlate
      type = "自车"
    }
    val registerDto = AccidentRegisterDto4Update().apply { cars = listOf(carDto) }

    // 2.2 执行数据添加
    val now = OffsetDateTime.now()
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据添加成功
    val carPo1 = em.createQuery("select c from AccidentCar c where c.parent.id = :pid", AccidentCar::class.java)
      .setParameter("pid", registerPo.id).singleResult
    assertNotNull(carPo1.id)
    assertEquals(carDto.sn, carPo1.sn)
    assertEquals(carDto.name, carPo1.name)
    assertEquals(carDto.type, carPo1.type)
    assertTrue(carPo1.updatedTime!!.isAfter(now))

    // 其他属性的值应为 null
    AccidentCar::class.memberProperties
      .filterNot { listOf("id", "sn", "name", "type", "parent", "updatedTime").contains(it.name) }
      .forEach { assertNull(it.get(carPo1)) }
  }

  @Test
  fun `Cars success update`() {
    val now = OffsetDateTime.now()
    // 1. 构建一条事故登记数据，包含一条当事车辆信息
    val happenTime = now.truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    val carPo = randomAccidentCar(parent = registerPo)
    registerPo.cars = setOf(carPo)
    em.flush();em.clear()

    // 2. 更新当事车辆数据
    // 2.1 构建数据
    val carDto = AccidentCarDto4Update().apply {
      id = carPo.id
      model = "出租车"
      towCount = randomInt(20, 30).toShort()
      damageMoney = BigDecimal("${randomInt(200, 300)}.00")
    }
    val registerDto = AccidentRegisterDto4Update().apply { cars = listOf(carDto) }

    // 2.2 执行数据更新
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据更新成功
    val updatedCarPo = em.createQuery("select c from AccidentCar c where c.parent.id = :pid", AccidentCar::class.java)
      .setParameter("pid", registerPo.id).singleResult
    assertEquals(carPo.id, updatedCarPo.id)

    // 如下属性应该更新为新的值：
    assertNotEquals(carPo.model, updatedCarPo.model)
    assertNotEquals(carPo.towCount, updatedCarPo.towCount)
    assertNotEquals(carPo.damageMoney, updatedCarPo.damageMoney)

    assertEquals(carDto.model, updatedCarPo.model)
    assertEquals(carDto.towCount, updatedCarPo.towCount)
    assertEquals(carDto.damageMoney, updatedCarPo.damageMoney)
    assertTrue(updatedCarPo.updatedTime!!.isAfter(now))

    // 其他属性应保持原值没有更新
    AccidentCar::class.memberProperties
      .filterNot { listOf("id", "parent", "updatedTime", "model", "towCount", "damageMoney").contains(it.name) }
      .forEach { assertEquals(it.get(carPo), it.get(updatedCarPo)) }
  }

  @Test
  fun `Cars success delete`() {
    val now = OffsetDateTime.now()
    // 1. 构建一条事故登记数据，包含2条当事车辆信息
    val happenTime = now.truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    val carPo1 = randomAccidentCar(parent = registerPo)
    val carPo2 = randomAccidentCar(parent = registerPo)
    registerPo.cars = setOf(carPo1, carPo2)
    em.flush();em.clear()

    // 2. 删除 1 条当事车辆
    val toKeepCarDto = AccidentCarDto4Update().apply { id = carPo1.id }
    var registerDto = AccidentRegisterDto4Update().apply { cars = listOf(toKeepCarDto) }

    // 2.2 执行数据删除
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据删除成功
    var result = em.createQuery("select c from AccidentCar c where c.parent.id = :pid", AccidentCar::class.java)
      .setParameter("pid", registerPo.id).resultList
    assertEquals(1, result.size)
    assertEquals(carPo1, result[0])

    // 3. 清空数据
    registerDto = AccidentRegisterDto4Update().apply { cars = listOf() } // 空的集合代表要清空数据
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 3.2 验证数据清空成功
    result = em.createQuery("select c from AccidentCar c where c.parent.id = :pid", AccidentCar::class.java)
      .setParameter("pid", registerPo.id).resultList
    assertEquals(0, result.size)
  }

  @Test
  fun `Peoples success create`() {
    // 1. 构建一条事故登记数据
    val happenTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    em.flush();em.clear()

    // 2. 添加当事人
    // 2.1 构建数据
    val peopleDto = AccidentPeopleDto4Update().apply {
      sn = 0
      name = registerPo.driverName
      type = "自车"
    }
    val registerDto = AccidentRegisterDto4Update().apply { peoples = listOf(peopleDto) }

    // 2.2 执行数据添加
    val now = OffsetDateTime.now()
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据添加成功
    val peoplePo1 = em.createQuery("select c from AccidentPeople c where c.parent.id = :pid", AccidentPeople::class.java)
      .setParameter("pid", registerPo.id).singleResult
    assertNotNull(peoplePo1.id)
    assertEquals(peopleDto.sn, peoplePo1.sn)
    assertEquals(peopleDto.name, peoplePo1.name)
    assertEquals(peopleDto.type, peoplePo1.type)
    assertTrue(peoplePo1.updatedTime!!.isAfter(now))

    // 其他属性的值应为 null
    AccidentPeople::class.memberProperties
      .filterNot { listOf("id", "sn", "name", "type", "parent", "updatedTime").contains(it.name) }
      .forEach { assertNull(it.get(peoplePo1)) }
  }

  @Test
  fun `Peoples success update`() {
    val now = OffsetDateTime.now()
    // 1. 构建一条事故登记数据，包含一条当事人信息
    val happenTime = now.truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    val peoplePo = randomAccidentPeople(parent = registerPo)
    registerPo.peoples = setOf(peoplePo)
    em.flush();em.clear()

    // 2. 更新当事人数据
    // 2.1 构建数据
    val peopleDto = AccidentPeopleDto4Update().apply {
      id = peoplePo.id
      name = random("driver")
      sex = Female
      damageMoney = BigDecimal("${randomInt(200, 300)}.00")
    }
    val registerDto = AccidentRegisterDto4Update().apply { peoples = listOf(peopleDto) }

    // 2.2 执行数据更新
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据更新成功
    val updatedPeoplePo = em.createQuery("select c from AccidentPeople c where c.parent.id = :pid", AccidentPeople::class.java)
      .setParameter("pid", registerPo.id).singleResult
    assertEquals(peoplePo.id, updatedPeoplePo.id)

    // 如下属性应该更新为新的值：
    assertNotEquals(peoplePo.name, updatedPeoplePo.name)
    assertNotEquals(peoplePo.sex, updatedPeoplePo.sex)
    assertNotEquals(peoplePo.damageMoney, updatedPeoplePo.damageMoney)

    assertEquals(peopleDto.name, updatedPeoplePo.name)
    assertEquals(peopleDto.sex, updatedPeoplePo.sex)
    assertEquals(peopleDto.damageMoney, updatedPeoplePo.damageMoney)
    assertTrue(updatedPeoplePo.updatedTime!!.isAfter(now))

    // 其他属性应保持原值没有更新
    AccidentPeople::class.memberProperties
      .filterNot { listOf("id", "parent", "updatedTime", "name", "sex", "damageMoney").contains(it.name) }
      .forEach { assertEquals(it.get(peoplePo), it.get(updatedPeoplePo)) }
  }

  @Test
  fun `Peoples success delete`() {
    val now = OffsetDateTime.now()
    // 1. 构建一条事故登记数据，包含2条当事人信息
    val happenTime = now.truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    val peoplePo1 = randomAccidentPeople(parent = registerPo)
    val peoplePo2 = randomAccidentPeople(parent = registerPo)
    registerPo.peoples = setOf(peoplePo1, peoplePo2)
    em.flush();em.clear()

    // 2. 删除 1 条当事人
    val toKeepPeopleDto = AccidentPeopleDto4Update().apply { id = peoplePo1.id }
    var registerDto = AccidentRegisterDto4Update().apply { peoples = listOf(toKeepPeopleDto) }

    // 2.2 执行数据删除
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据删除成功
    var result = em.createQuery("select c from AccidentPeople c where c.parent.id = :pid", AccidentPeople::class.java)
      .setParameter("pid", registerPo.id).resultList
    assertEquals(1, result.size)
    assertEquals(peoplePo1, result[0])

    // 3. 清空数据
    registerDto = AccidentRegisterDto4Update().apply { peoples = listOf() } // 空的集合代表要清空数据
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 3.2 验证数据清空成功
    result = em.createQuery("select c from AccidentPeople c where c.parent.id = :pid", AccidentPeople::class.java)
      .setParameter("pid", registerPo.id).resultList
    assertEquals(0, result.size)
  }

  @Test
  fun `Others success create`() {
    // 1. 构建一条事故登记数据
    val happenTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    em.flush();em.clear()

    // 2. 添加其他物体
    // 2.1 构建数据
    val otherDto = AccidentOtherDto4Update().apply {
      sn = 0
      name = random("name")
      type = "自车"
    }
    val registerDto = AccidentRegisterDto4Update().apply { others = listOf(otherDto) }

    // 2.2 执行数据添加
    val now = OffsetDateTime.now()
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据添加成功
    val otherPo1 = em.createQuery("select c from AccidentOther c where c.parent.id = :pid", AccidentOther::class.java)
      .setParameter("pid", registerPo.id).singleResult
    assertNotNull(otherPo1.id)
    assertEquals(otherDto.sn, otherPo1.sn)
    assertEquals(otherDto.name, otherPo1.name)
    assertEquals(otherDto.type, otherPo1.type)
    assertTrue(otherPo1.updatedTime!!.isAfter(now))

    // 其他属性的值应为 null
    AccidentOther::class.memberProperties
      .filterNot { listOf("id", "sn", "name", "type", "parent", "updatedTime").contains(it.name) }
      .forEach { assertNull(it.get(otherPo1)) }
  }

  @Test
  fun `Others success update`() {
    val now = OffsetDateTime.now()
    // 1. 构建一条事故登记数据，包含一条其他物体信息
    val happenTime = now.truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    val otherPo = randomAccidentOther(parent = registerPo)
    registerPo.others = setOf(otherPo)
    em.flush();em.clear()

    // 2. 更新其他物体数据
    // 2.1 构建数据
    val otherDto = AccidentOtherDto4Update().apply {
      id = otherPo.id
      name = random("name")
      damageMoney = BigDecimal("${randomInt(200, 300)}.00")
    }
    val registerDto = AccidentRegisterDto4Update().apply { others = listOf(otherDto) }

    // 2.2 执行数据更新
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据更新成功
    val updatedOtherPo = em.createQuery("select c from AccidentOther c where c.parent.id = :pid", AccidentOther::class.java)
      .setParameter("pid", registerPo.id).singleResult
    assertEquals(otherPo.id, updatedOtherPo.id)

    // 如下属性应该更新为新的值：
    assertNotEquals(otherPo.name, updatedOtherPo.name)
    assertNotEquals(otherPo.damageMoney, updatedOtherPo.damageMoney)

    assertEquals(otherDto.name, updatedOtherPo.name)
    assertEquals(otherDto.damageMoney, updatedOtherPo.damageMoney)
    assertTrue(updatedOtherPo.updatedTime!!.isAfter(now))

    // 其他属性应保持原值没有更新
    AccidentOther::class.memberProperties
      .filterNot { listOf("id", "parent", "updatedTime", "name", "damageMoney").contains(it.name) }
      .forEach { assertEquals(it.get(otherPo), it.get(updatedOtherPo)) }
  }

  @Test
  fun `Others success delete`() {
    val now = OffsetDateTime.now()
    // 1. 构建一条事故登记数据，包含2条其他物体信息
    val happenTime = now.truncatedTo(ChronoUnit.MINUTES)
    val registerPo = randomRegister(
      status = Status.Draft,
      happenTime = happenTime
    )
    val otherPo1 = randomAccidentOther(parent = registerPo)
    val otherPo2 = randomAccidentOther(parent = registerPo)
    registerPo.others = setOf(otherPo1, otherPo2)
    em.flush();em.clear()

    // 2. 删除 1 条其他物体
    val toKeepOtherDto = AccidentOtherDto4Update().apply { id = otherPo1.id }
    var registerDto = AccidentRegisterDto4Update().apply { others = listOf(toKeepOtherDto) }

    // 2.2 执行数据删除
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 2.3 验证数据删除成功
    var result = em.createQuery("select c from AccidentOther c where c.parent.id = :pid", AccidentOther::class.java)
      .setParameter("pid", registerPo.id).resultList
    assertEquals(1, result.size)
    assertEquals(otherPo1, result[0])

    // 3. 清空数据
    registerDto = AccidentRegisterDto4Update().apply { others = listOf() } // 空的集合代表要清空数据
    StepVerifier.create(dao.update(registerPo.id!!, registerDto.data)).expectNext(true).verifyComplete()
    em.flush();em.clear()

    // 3.2 验证数据清空成功
    result = em.createQuery("select c from AccidentOther c where c.parent.id = :pid", AccidentOther::class.java)
      .setParameter("pid", registerPo.id).resultList
    assertEquals(0, result.size)
  }
}