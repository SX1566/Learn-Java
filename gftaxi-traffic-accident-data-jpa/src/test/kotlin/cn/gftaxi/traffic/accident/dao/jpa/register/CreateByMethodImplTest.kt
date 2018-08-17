package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentRegisterDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.random
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentRegisterDaoImpl.createBy].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class CreateByMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  @Test
  fun success() {
    // init data
    val happenTime = OffsetDateTime.now().minusHours(6)
    val accidentDraft = randomAccidentDraft(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentDraft.Status.Todo,
      happenTime = happenTime,
      overdue = false,
      carPlate = random("粤A."),
      driverName = random("driver")
    )
    em.persist(accidentDraft)
    em.flush()
    em.clear()

    // invoke
    val accidentRegister = dao.createBy(accidentDraft)
    em.flush()
    em.clear()

    // verify
    var result: AccidentRegister? = null
    StepVerifier.create(accidentRegister)
      .consumeNextWith {
        result = it
        assertEquals(accidentDraft, it.draft)

        // 验证复制的字段
        assertEquals(accidentDraft.id, it.id)
        assertEquals(accidentDraft.driverName, it.driverName)
        assertEquals(accidentDraft.carPlate, it.carPlate)
        assertEquals(accidentDraft.happenTime, it.happenTime)
        assertEquals(accidentDraft.hitForm, it.hitForm)
        assertEquals(accidentDraft.hitType, it.hitType)
        assertEquals(accidentDraft.location, it.location)
        assertEquals(accidentDraft.describe, it.describe)

        // 验证草稿状态
        assertEquals(AccidentRegister.Status.Draft, it.status)

        // 验证自动创建了一条自车类型的当事车辆信息
        assertEquals(1, it.cars!!.size)
        val car = it.cars!!.first()
        assertEquals(0, car.sn)
        assertEquals(it.carPlate, car.name)
        assertEquals("自车", car.type)
        assertEquals(it, car.parent)

        // 验证自动创建了一条自车类型的当事人信息
        assertEquals(1, it.peoples!!.size)
        val people = it.peoples!!.first()
        assertEquals(0, people.sn)
        assertEquals(it.driverName, people.name)
        assertEquals("自车", people.type)
        assertEquals(it, people.parent)

        // 验证车辆冗余字段信息 TODO

        // 验证司机冗余字段信息 TODO

        // 验证历史统计信息 TODO

        // 验证事发地点的省级、地级、县级的解析 TODO
      }.verifyComplete()

    // 验证数据库已经生成了符合要求的事故登记信息
    assertEquals(result, em.find(AccidentRegister::class.java, accidentDraft.id))
  }
}