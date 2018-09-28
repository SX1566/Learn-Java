package cn.gftaxi.traffic.accident.dao.jpa.report

import cn.gftaxi.traffic.accident.dao.jpa.AccidentReportDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.random
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentPeople
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegister
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentReport
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomInt
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentPeople
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.operation.OperationType
import tech.simter.operation.po.Attachment
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Operator
import tech.simter.operation.po.Target
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test [AccidentReportDaoImpl.find].
 *
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
internal class FindMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentReportDao
) {
  fun randomReportOperation(
    type: String,
    targetReport: AccidentReport,
    time: OffsetDateTime = OffsetDateTime.now(),
    attachmentsSize: Int? = null
  ): Operation {
    return Operation(type = type,
      target = Target(type = AccidentReport::class.simpleName!!, id = targetReport.id.toString()),
      time = time,
      attachments = attachmentsSize?.let {
        MutableList(it) {
          Attachment(
            id = random("attachmentId"), name = random("name"),
            size = randomInt(10, 1000), ext = random("ext")
          )
        }
      },
      operator = Operator(id = random("id"), name = random("name"))
    )
  }

  @Test
  fun foundNothing() {
    StepVerifier.create(dao.find())
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(0, page.totalElements)
        assertTrue(page.content.isEmpty())
      }.verifyComplete()
  }

  val data = mutableListOf<
    Triple<Triple<AccidentDraft, AccidentRegister, AccidentReport?>, AccidentPeople, Pair<Int, Operation?>>
    >()


  fun initData() {
    // 构造一条审核通过的事故报告
    em.run {
      val draft = randomAccidentDraft(
        code = "20180101_01",
        status = AccidentDraft.Status.Todo,
        happenTime = OffsetDateTime.now()
      )
      persist(draft)
      val register = randomAccidentRegister(
        draft = draft,
        status = AccidentRegister.Status.Approved,
        driverType = AccidentRegister.DriverType.Official
      )
      persist(register)
      val report = randomAccidentReport(register = register, status = AccidentReport.Status.Approved)
      persist(report)
      val people1 = randomAccidentPeople(parent = register, type = "自车")
      persist(people1)
      val people2 = randomAccidentPeople(parent = register, type = "三者")
      persist(people2)
      val people3 = randomAccidentPeople(parent = register, type = "三者")
      persist(people3)
      val operation1 = randomReportOperation(
        type = OperationType.Approval.toString(),
        targetReport = report,
        time = OffsetDateTime.now().plusDays(1)
      )
      persist(operation1)
      val operation2 = randomReportOperation(
        type = OperationType.Rejection.toString(),
        targetReport = report,
        attachmentsSize = 2
      )
      persist(operation2)
      val operation3 = randomReportOperation(
        type = OperationType.Rejection.toString(),
        targetReport = report,
        attachmentsSize = 1
      )
      persist(operation3)
      data.add(Triple(Triple(draft, register, report), people1, Pair(3, operation1)))
    }
    // 构造一条审核不通过的事故报告
    em.run {
      val draft = randomAccidentDraft(
        code = "20180101_02",
        status = AccidentDraft.Status.Todo,
        happenTime = OffsetDateTime.now().minusDays(1)
      )
      persist(draft)
      val register = randomAccidentRegister(
        draft = draft,
        status = AccidentRegister.Status.Approved,
        driverType = AccidentRegister.DriverType.Official
      )
      persist(register)
      val report = randomAccidentReport(register = register, status = AccidentReport.Status.Rejected)
      persist(report)
      val people1 = randomAccidentPeople(parent = register, type = "自车")
      persist(people1)
      val people2 = randomAccidentPeople(parent = register, type = "三者")
      persist(people2)
      val people3 = randomAccidentPeople(parent = register, type = "三者")
      persist(people3)
      val operation1 = randomReportOperation(
        type = OperationType.Rejection.toString(),
        targetReport = report,
        time = OffsetDateTime.now().plusDays(1),
        attachmentsSize = 3
      )
      persist(operation1)
      val operation2 = randomReportOperation(
        type = OperationType.Rejection.toString(),
        targetReport = report,
        attachmentsSize = 2
      )
      persist(operation2)
      data.add(Triple(Triple(draft, register, report), people1, Pair(2, operation1)))
    }
    // 构造一条待审核的事故报告
    em.run {
      val draft = randomAccidentDraft(
        code = "20180101_03",
        status = AccidentDraft.Status.Todo,
        happenTime = OffsetDateTime.now().minusDays(2)
      )
      persist(draft)
      val register = randomAccidentRegister(
        draft = draft,
        status = AccidentRegister.Status.Approved,
        driverType = AccidentRegister.DriverType.Official
      )
      persist(register)
      val report = randomAccidentReport(register = register, status = AccidentReport.Status.ToCheck)
      persist(report)
      val people1 = randomAccidentPeople(parent = register, type = "自车")
      persist(people1)
      val people2 = randomAccidentPeople(parent = register, type = "三者")
      persist(people2)
      val people3 = randomAccidentPeople(parent = register, type = "三者")
      persist(people3)
      val operation = randomReportOperation(
        type = OperationType.Confirmation.toString(),
        targetReport = report,
        time = OffsetDateTime.now().plusDays(1)
      )
      persist(operation)
      data.add(Triple(Triple(draft, register, report), people1, Pair(0, null)))
    }
    // 构造一条待报告有草稿的事故报告
    em.run {
      val draft = randomAccidentDraft(
        code = "20180101_04",
        status = AccidentDraft.Status.Todo,
        happenTime = OffsetDateTime.now().minusDays(3)
      )
      persist(draft)
      val register = randomAccidentRegister(
        draft = draft,
        status = AccidentRegister.Status.Approved,
        driverType = AccidentRegister.DriverType.Official
      )
      persist(register)
      val report = randomAccidentReport(register = register, status = AccidentReport.Status.Draft)
      persist(report)
      val people1 = randomAccidentPeople(parent = register, type = "自车")
      persist(people1)
      val people2 = randomAccidentPeople(parent = register, type = "三者")
      persist(people2)
      val people3 = randomAccidentPeople(parent = register, type = "三者")
      persist(people3)
      data.add(Triple(Triple(draft, register, report), people1, Pair(0, null)))
    }
    // 构造一条待报告无草稿的事故报告
    em.run {
      val draft = randomAccidentDraft(
        code = "20180101_05",
        status = AccidentDraft.Status.Todo,
        happenTime = OffsetDateTime.now().minusDays(4)
      )
      persist(draft)
      val register = randomAccidentRegister(
        draft = draft,
        status = AccidentRegister.Status.Approved,
        driverType = AccidentRegister.DriverType.Official
      )
      persist(register)
      val people1 = randomAccidentPeople(parent = register, type = "自车")
      persist(people1)
      val people2 = randomAccidentPeople(parent = register, type = "三者")
      persist(people2)
      val people3 = randomAccidentPeople(parent = register, type = "三者")
      persist(people3)
      data.add(Triple(Triple(draft, register, null), people1, Pair(0, null)))
    }
    // 以上5条事故的案发时间严格递减
    em.flush()
    em.clear()
  }

  fun verifyDto(index: Int, dto: AccidentReportDto4View) {
    assertEquals(data[index].first.second.id, dto.id)
    assertEquals(data[index].first.first.code, dto.code)
    assertEquals(data[index].first.third?.status, dto.reportStatus)
    assertEquals(data[index].first.second.motorcadeName, dto.motorcadeName)
    assertEquals(data[index].first.second.carPlate, dto.carPlate)
    assertEquals(data[index].first.second.carModel, dto.carModel)
    assertEquals(data[index].first.second.driverName, dto.driverName)
    assertEquals(data[index].first.second.driverType, dto.driverType)
    assertEquals(data[index].first.second.happenTime, dto.happenTime)
    assertEquals(data[index].first.first.location, dto.location)
    assertEquals(data[index].first.second.level, dto.level)
    assertEquals(data[index].first.second.hitForm, dto.hitForm)
    assertEquals(data[index].second.duty, dto.duty)
    assertEquals(data[index].first.first.draftTime, dto.draftTime)
    assertEquals(data[index].first.first.overdueDraft, dto.overdueDraft)
    assertEquals(data[index].first.second.registerTime, dto.registerTime)
    assertEquals(data[index].first.second.overdueRegister, dto.overdueRegister)
    assertEquals(data[index].first.third?.reportTime, dto.reportTime)
    assertEquals(data[index].first.third?.overdueReport, dto.overdueReport)
    assertEquals(data[index].first.third?.appointDriverReturnTime, dto.appointDriverReturnTime)
    assertEquals(data[index].third.first, dto.checkedCount)
    assertEquals(data[index].third.second?.comment, dto.checkedComment)
    assertEquals(data[index].third.second?.attachments, dto.checkedAttachments)
  }

  @Test
  fun find() {
    initData()

    // 获取所有状态(statuses 为空)
    StepVerifier.create(dao.find(statuses = null))
      .consumeNextWith {
        assertEquals(0, it.number)
        assertEquals(25, it.size)
        assertEquals(5, it.totalElements)
        it.content.forEachIndexed { index, accidentReportDto4View ->
          verifyDto(index, accidentReportDto4View)
        }
      }.verifyComplete()

    // 获取所有状态(statuses 为空数组)
    StepVerifier.create(dao.find(statuses = listOf()))
      .consumeNextWith {
        assertEquals(0, it.number)
        assertEquals(25, it.size)
        assertEquals(5, it.totalElements)
        it.content.forEachIndexed { index, accidentReportDto4View ->
          verifyDto(index, accidentReportDto4View)
        }
      }.verifyComplete()

    // 获取审核通过状态
    StepVerifier.create(dao.find(statuses = listOf(AccidentReport.Status.Approved)))
      .consumeNextWith {
        assertEquals(0, it.number)
        assertEquals(25, it.size)
        assertEquals(1, it.totalElements)
        it.content.forEachIndexed { index, accidentReportDto4View ->
          verifyDto(index, accidentReportDto4View)
        }
      }.verifyComplete()

    // 获取审核不通过状态
    StepVerifier.create(dao.find(statuses = listOf(AccidentReport.Status.Rejected)))
      .consumeNextWith {
        assertEquals(0, it.number)
        assertEquals(25, it.size)
        assertEquals(1, it.totalElements)
        it.content.forEachIndexed { index, accidentReportDto4View ->
          verifyDto(index + 1, accidentReportDto4View)
        }
      }.verifyComplete()

    // 获取待审核状态
    StepVerifier.create(dao.find(statuses = listOf(AccidentReport.Status.ToCheck)))
      .consumeNextWith {
        assertEquals(0, it.number)
        assertEquals(25, it.size)
        assertEquals(1, it.totalElements)
        it.content.forEachIndexed { index, accidentReportDto4View ->
          verifyDto(index + 2, accidentReportDto4View)
        }
      }.verifyComplete()

    // 获取待报告状态
    StepVerifier.create(dao.find(statuses = listOf(AccidentReport.Status.Draft)))
      .consumeNextWith {
        assertEquals(0, it.number)
        assertEquals(25, it.size)
        assertEquals(2, it.totalElements)
        it.content.forEachIndexed { index, accidentReportDto4View ->
          verifyDto(index + 3, accidentReportDto4View)
        }
      }.verifyComplete()

    // 获取多状态(待报告 + 待审核)
    StepVerifier.create(
      dao.find(statuses = listOf(AccidentReport.Status.Draft, AccidentReport.Status.ToCheck))
    ).consumeNextWith {
      assertEquals(0, it.number)
      assertEquals(25, it.size)
      assertEquals(3, it.totalElements)
      it.content.forEachIndexed { index, accidentReportDto4View ->
        verifyDto(index + 2, accidentReportDto4View)
      }
    }.verifyComplete()
  }
}