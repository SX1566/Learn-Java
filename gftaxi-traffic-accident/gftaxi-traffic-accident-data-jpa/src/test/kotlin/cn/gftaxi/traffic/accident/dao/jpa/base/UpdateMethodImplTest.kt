package cn.gftaxi.traffic.accident.dao.jpa.base

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_DRAFT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.common.toJson
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dto.AccidentCarDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentPeopleDto4Form
import cn.gftaxi.traffic.accident.po.AccidentCar
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentCar
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentOther
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentPeople
import cn.gftaxi.traffic.accident.test.TestUtils.randomAuthenticatedUser
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import cn.gftaxi.traffic.accident.test.TestUtils.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tech.simter.exception.NotFoundException
import tech.simter.operation.OperationType.Modification
import tech.simter.operation.dao.OperationDao
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

/**
 * Test [AccidentDaoImpl.update].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
class UpdateMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val caseRepository: AccidentCaseJpaRepository,
  private val dao: AccidentDao,
  private val operationDao: OperationDao
) {
  @Test
  fun `Do nothing because empty data`() {
    StepVerifier.create(dao.update(randomInt(), mapOf()))
      .verifyComplete()
  }

  @Test
  fun `Failed by NotFound`() {
    StepVerifier.create(dao.update(randomInt(), mapOf("level" to randomString())))
      .expectError(NotFoundException::class.java)
      .verify()
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun success() {
    // 1. init data
    var oldCase = randomCase().first
    oldCase.run {
      cars = List(2) { randomAccidentCar(parent = this, sn = it.toShort(), type = "自车") }
      peoples = List(2) { randomAccidentPeople(parent = this, sn = it.toShort(), type = "自车") }
      others = List(2) { randomAccidentOther(parent = this, sn = it.toShort(), type = "自车") }
    }
    oldCase = caseRepository.save(oldCase)
    val caseId = oldCase.id!!
    val user = randomAuthenticatedUser()
    /* 1.1. 构造一条要更新的数据：修改部分基本属性（囊括所有数据类型）；删除所有的当事车辆；
     * 当事人更新一行，保留一行，新建一行；保留所有其他物体。
     */
    val data = mapOf(
      "motorcadeName" to randomString("change"),
      "historyAccidentCount" to 4.toShort(),
      "actualDriverReturnTime" to OffsetDateTime.now().plusDays(1),
      "cars" to null,
      "peoples" to listOf(
        AccidentPeopleDto4Form().apply {
          id = oldCase.peoples!![0].id
          followType = randomString("followType")
        },
        AccidentPeopleDto4Form().apply {
          id = oldCase.peoples!![1].id
        },
        AccidentPeopleDto4Form().apply {
          name = randomString("name")
          sn = 4
          type = "三者"
        }
      )
    )
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(user)))

    // 2. 验证更新的步骤正确性
    StepVerifier.create(dao.update(id = caseId, data = data,
      targetType = ACCIDENT_DRAFT_TARGET_TYPE, generateLog = true))
      .verifyComplete()

    // 3. 验证更新后的数据正确性
    val peopledtos = data["peoples"] as List<AccidentPeopleDto4Form>
    val newCase = caseRepository.findById(caseId).get()
    newCase.data.forEach {
      val key = it.key
      when {
      // 3.1. 验证当事车辆，无当事车辆
        key == "cars" -> assertEquals(listOf<AccidentCar>(), newCase.cars)
      // 3.2. 验证其他物体，数据未更新
        key == "others" -> newCase.others!!.forEachIndexed { index, po ->
          assertEquals(oldCase.others!![index].data.filter { it.key != "parent" },
            po.data.filter { it.key != "parent" })
        }
      // 3.3. 验证当事人，第一行数据更新，第二行数据未更新，第三行数据为新建
        key == "peoples" -> {
          assertEquals(
            oldCase.peoples!![0].data.filter { !listOf("parent", "updatedTime", "followType").contains(it.key) },
            newCase.peoples!![0].data.filter { !listOf("parent", "updatedTime", "followType").contains(it.key) }
          )
          assertEquals(peopledtos[0].followType, newCase.peoples!![0].data["followType"])
          assertEquals(oldCase.peoples!![1].data.filter { it.key != "parent" },
            newCase.peoples!![1].data.filter { it.key != "parent" })
          newCase.peoples!![2].data.forEach {
            if (peopledtos[2].data.containsKey(it.key)) {
              assertEquals(peopledtos[2].data[it.key], it.value)
            } else if (!listOf("parent", "updatedTime").contains(it.key)) {
              assertEquals(null, it.value)
            }
          }
        }
        data.containsKey(key) -> assertEquals(data[key], newCase.data[key])
        else -> assertEquals(oldCase.data[key], newCase.data[key])
      }
    }

    // 4 验证日志数据正确性
    StepVerifier.create(operationDao.findByCluster("$ACCIDENT_OPERATION_CLUSTER-$caseId"))
      .consumeNextWith {

        // 4.1 验证日志行的基本属性
        assertEquals(Modification.name, it.type)
        assertEquals("$ACCIDENT_OPERATION_CLUSTER-$caseId", it.cluster)
        assertEquals(user.toOperator(), it.operator)
        assertEquals(OffsetDateTime.now().isBefore(it.time), false)
        assertEquals(caseId.toString(), it.target.id)
        assertEquals(ACCIDENT_DRAFT_TARGET_TYPE, it.target.type)
        assertEquals(operationTitles[Modification.name +ACCIDENT_DRAFT_TARGET_TYPE], it.title)

        // 4.1 验证更新的字段是否被正确记录
        val fields = it.fields!!

        assertEquals(7, fields.size)
        assertEquals("motorcadeName", fields[0].id)
        assertEquals("事发车队名称", fields[0].name)
        assertEquals("String", fields[0].type)
        assertEquals(oldCase.motorcadeName, fields[0].oldValue)
        assertEquals(newCase.motorcadeName, fields[0].newValue)

        assertEquals("historyAccidentCount", fields[1].id)
        assertEquals("历史事故宗数", fields[1].name)
        assertEquals("Short", fields[1].type)
        assertEquals(oldCase.historyAccidentCount?.toString(), fields[1].oldValue)
        assertEquals(newCase.historyAccidentCount?.toString(), fields[1].newValue)

        assertEquals("actualDriverReturnTime", fields[2].id)
        assertEquals("司机实际回队时间", fields[2].name)
        assertEquals("OffsetDateTime", fields[2].type)
        assertEquals(oldCase.actualDriverReturnTime?.format(FORMAT_DATE_TIME_TO_MINUTE), fields[2].oldValue)
        assertEquals(newCase.actualDriverReturnTime?.format(FORMAT_DATE_TIME_TO_MINUTE), fields[2].newValue)

        val car0 = oldCase.cars!![0]
        assertEquals("cars.${car0.id}", fields[3].id)
        assertEquals("删除自车${car0.name}", fields[3].name)
        assertEquals("AccidentCar", fields[3].type)
        assertEquals(car0.toJson { AccidentCarDto4Form.propertieNames.contains(it.key) }, fields[3].oldValue)
        assertEquals(null, fields[3].newValue)

        val car1 = oldCase.cars!![1]
        assertEquals("cars.${car1.id}", fields[4].id)
        assertEquals("删除自车${car1.name}", fields[4].name)
        assertEquals("AccidentCar", fields[4].type)
        assertEquals(car1.toJson { AccidentCarDto4Form.propertieNames.contains(it.key) }, fields[4].oldValue)
        assertEquals(null, fields[4].newValue)

        val oldPeople0 = oldCase.peoples!![0]
        val newPeople0 = newCase.peoples!![0]
        assertEquals("peoples.${oldPeople0.id}", fields[5].id)
        assertEquals("修改自车${oldPeople0.name}", fields[5].name)
        assertEquals("AccidentPeople", fields[5].type)
        assertEquals(oldPeople0.toJson { it.key == "followType" || it.key == "id" }, fields[5].oldValue)
        assertEquals(newPeople0.toJson { it.key == "followType" || it.key == "id" }, fields[5].newValue)

        val newPeople2 = newCase.peoples!![2]
        assertEquals("peoples.${newPeople2.id}", fields[6].id)
        assertEquals("新增三者${newPeople2.name}", fields[6].name)
        assertEquals("AccidentPeople", fields[6].type)
        assertEquals(null, fields[6].oldValue)
        assertEquals(newPeople2.toJson { listOf("sn", "name", "type", "id").contains(it.key) }, fields[6].newValue)
      }
      .verifyComplete()
  }
}