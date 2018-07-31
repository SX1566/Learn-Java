package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegisterRecord4EachStatus
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.Approval
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.Rejection
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
 * Test [AccidentRegisterDao.findChecked].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class FindCheckedMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  lateinit var onlyReportRecord: AccidentDraft
  lateinit var accidentRegisterRecords: Map<Status, Pair<AccidentRegister, Map<OperationType, AccidentOperation>>>

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

    // 1. 仅查审核通过
    StepVerifier.create(dao.findChecked(status = Approved))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(1, page.totalElements)

        // 审核通过案件 1 宗
        val record = accidentRegisterRecords[Approved]!!
        verifyDetail(
          actualDto = page.content[0],
          expectedDraft = record.first.draft,
          expectedRegister = record.first,
          expectedLastCheckedOperation = record.second[Approval]!!
        )
      }
      .verifyComplete()

    // 2. 仅查审核不通过
    StepVerifier.create(dao.findChecked(status = Rejected))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(1, page.totalElements)

        // 审核不通过案件 1 宗
        val record = accidentRegisterRecords[Rejected]!!
        verifyDetail(
          actualDto = page.content[0],
          expectedDraft = record.first.draft,
          expectedRegister = record.first,
          expectedLastCheckedOperation = record.second[Rejection]!!
        )
      }
      .verifyComplete()

    // 3. 两者都查
    StepVerifier.create(dao.findChecked())
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(2, page.totalElements)

        // 审核不通过案件 1 宗
        var record = accidentRegisterRecords[Rejected]!!
        verifyDetail(
          actualDto = page.content[0],
          expectedDraft = record.first.draft,
          expectedRegister = record.first,
          expectedLastCheckedOperation = record.second[Rejection]!!
        )

        // 审核通过案件 1 宗
        record = accidentRegisterRecords[Approved]!!
        verifyDetail(
          actualDto = page.content[1],
          expectedDraft = record.first.draft,
          expectedRegister = record.first,
          expectedLastCheckedOperation = record.second[Approval]!!
        )
      }
      .verifyComplete()
  }

  // dto 的每个属性都要验证
  private fun verifyDetail(actualDto: AccidentRegisterDto4Checked,
                           expectedDraft: AccidentDraft,
                           expectedRegister: AccidentRegister,
                           expectedLastCheckedOperation: AccidentOperation) {
    // 验证事故报案信息
    assertEquals(expectedDraft.id, actualDto.id)
    assertEquals(expectedDraft.code, actualDto.code)
    assertEquals(expectedDraft.carPlate, actualDto.carPlate)
    assertEquals(expectedDraft.driverName, actualDto.driverName)
    assertEquals(expectedDraft.happenTime, actualDto.happenTime)

    // 验证事故登记信息
    assertEquals(expectedRegister.driverType, actualDto.driverType)

    // 验证审核信息
    assertEquals(expectedLastCheckedOperation.operateTime, actualDto.checkedTime)
    assertEquals(expectedRegister.status, actualDto.checkedResult)
    assertEquals(expectedLastCheckedOperation.operatorName, actualDto.checkerName)
    assertEquals(1, actualDto.checkedCount)
  }

  @Test
  fun findWithInValidStatus() {
    assertThrows(IllegalArgumentException::class.java, {
      dao.findChecked(status = Status.Draft).subscribe()
    })
    assertThrows(IllegalArgumentException::class.java, {
      dao.findChecked(status = Status.ToCheck).subscribe()
    })
  }
}