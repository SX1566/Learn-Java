package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentRegisterDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegisterRecord4EachStatus
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.Confirmation
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.ToCheck
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentRegisterDaoImpl.findTodo].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class FindTodoMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  lateinit var onlyReportRecord: AccidentDraft
  lateinit var accidentRegisterRecords:
    Map<Status, Triple<AccidentRegister, AccidentDraft, Map<OperationType, AccidentOperation>>>

  // 构建初始化数据
  fun initData() {
    val baseTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)

    // 仅报案案件(未有登记信息) 1 宗
    onlyReportRecord = randomAccidentDraft(
      code = nextCode(baseTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentDraft.Status.Todo,
      happenTime = baseTime,
      overdue = false
    )
    em.persist(onlyReportRecord)

    // 各个状态的事故登记信息都初始化 1 条数据
    accidentRegisterRecords = randomAccidentRegisterRecord4EachStatus(
      em = em,
      baseTime = baseTime.plusHours(-1),
      positive = false
    )

    em.flush()
    em.clear()
  }

  @Test
  fun findWithValidStatus() {
    // 构建初始化数据：各种状态的数据都创建 1 条
    initData()

    // 1. 仅查待登记
    StepVerifier.create(dao.findTodo(status = Draft))
      // 仅报案案件(未有登记信息) 1 宗
      .consumeNextWith {
        verifyDetail(
          actualDto = it,
          expectedDraft = onlyReportRecord
        )
      }
      // 草稿案件 1 宗
      .consumeNextWith {
        val record = accidentRegisterRecords[Draft]!!
        verifyDetail(
          actualDto = it,
          expectedDraft = record.second,
          expectedRegister = record.first
        )
      }
      .verifyComplete()

    // 2. 仅查待审
    StepVerifier.create(dao.findTodo(status = ToCheck))
      // 待审核案件 1 宗
      .consumeNextWith {
        val record = accidentRegisterRecords[ToCheck]!!
        verifyDetail(
          actualDto = it,
          expectedDraft = record.second,
          expectedRegister = record.first,
          expectedLastSubmitOperation = record.third[Confirmation]
        )
      }
      .verifyComplete()

    // 3. 两者都查
    StepVerifier.create(dao.findTodo())
      // 仅报案案件(未有登记信息) 1 宗
      .consumeNextWith {
        verifyDetail(
          actualDto = it,
          expectedDraft = onlyReportRecord
        )
      }
      // 草稿案件 1 宗
      .consumeNextWith {
        val record = accidentRegisterRecords[Draft]!!
        verifyDetail(
          actualDto = it,
          expectedDraft = record.second,
          expectedRegister = record.first
        )
      }
      // 待审核案件 1 宗
      .consumeNextWith {
        val record = accidentRegisterRecords[ToCheck]!!
        verifyDetail(
          actualDto = it,
          expectedDraft = record.second,
          expectedRegister = record.first,
          expectedLastSubmitOperation = record.third[Confirmation]
        )
      }
      .verifyComplete()
  }

  // dto 的每个属性都要验证
  private fun verifyDetail(actualDto: AccidentRegisterDto4Todo,
                           expectedDraft: AccidentDraft,
                           expectedRegister: AccidentRegister? = null,
                           expectedLastSubmitOperation: AccidentOperation? = null) {
    // 验证事故报案信息
    assertEquals(expectedDraft.id, actualDto.id)
    assertEquals(expectedDraft.code, actualDto.code)
    assertEquals(expectedDraft.carPlate, actualDto.carPlate)
    assertEquals(expectedDraft.driverName, actualDto.driverName)
    assertEquals(expectedDraft.happenTime, actualDto.happenTime)
    assertEquals(expectedDraft.authorId, actualDto.authorId)
    assertEquals(expectedDraft.authorName, actualDto.authorName)
    assertEquals(expectedDraft.hitForm, actualDto.hitForm)
    assertEquals(expectedDraft.hitType, actualDto.hitType)
    assertEquals(expectedDraft.reportTime, actualDto.reportTime)
    assertEquals(expectedDraft.overdue, actualDto.overdueReport)

    // 验证事故登记信息
    if (expectedRegister == null || expectedRegister.status == Status.Draft) {
      if (expectedRegister == null) assertNull(actualDto.driverType)
      else assertEquals(expectedRegister.driverType, actualDto.driverType)
      assertNull(actualDto.registerTime)
      assertNull(actualDto.overdueRegister)
      assertNull(actualDto.submitTime)
      assertNull(expectedLastSubmitOperation)
    } else {
      assertEquals(expectedRegister.driverType, actualDto.driverType)
      assertEquals(expectedRegister.registerTime, actualDto.registerTime)
      assertEquals(expectedRegister.overdue, actualDto.overdueRegister)
      // 验证提交审核信息
      if (expectedLastSubmitOperation != null) {
        assertEquals(expectedLastSubmitOperation.operateTime, actualDto.submitTime)
      }
    }
  }

  @Test
  fun findWithInValidStatus() {
    assertThrows(IllegalArgumentException::class.java, { dao.findTodo(Status.Approved).subscribe() })
    assertThrows(IllegalArgumentException::class.java, { dao.findTodo(Status.Rejected).subscribe() })
  }
}