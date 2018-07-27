package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.view.CarMan
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 测试事故登记 Dao 实现。
 *
 * @author JF
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class AccidentRegisterDaoImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  @Test
  fun statSummary() {
    // mock
    val now = OffsetDateTime.now()
    // 构建本月的事故报案数据
    initData4StatSummary(now)
    val expected1 = AccidentRegisterDto4StatSummary(
      scope = "本月", total = 90, checked = 30, checking = 30, drafting = 30, overdueDraft = 30, overdueRegister = 15
    )
    // 构建上个月的事故报案数据
    initData4StatSummary(now.minusMonths(1))
    val expected2 = AccidentRegisterDto4StatSummary(
      scope = "上月", total = 90, checked = 30, checking = 30, drafting = 30, overdueDraft = 30, overdueRegister = 15
    )
    // 本年的事故报案数据：本月 + 上个月
    val expected3 = AccidentRegisterDto4StatSummary(
      scope = "本年", total = 180, checked = 60, checking = 60, drafting = 60, overdueDraft = 60, overdueRegister = 30
    )
    em.flush(); em.clear()

    // invoke
    val actual = dao.statSummary()

    // verify
    StepVerifier.create(actual)
      .expectNext(expected1)
      .expectNext(expected2)
      .expectNext(expected3)
      .verifyComplete()
  }

  // 构建指定时间范围的事故报案：总数 90，其中已登已审 30，已登在审 30(包含逾期登记 15)，尚未登记 30(包含逾期报案 30)
  private fun initData4StatSummary(scope: OffsetDateTime) {
    val ymd = scope.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    // 尚未登记且逾期报案：30
    for (i in 1..30) {
      createData("${ymd}_$i", "car$i", "driver$i", scope, AccidentDraft.Status.Todo, true)
    }
    // 已登已审案件：30
    for (i in 31..60) {
      createData("${ymd}_$i", "car$i", "driver$i", scope, AccidentDraft.Status.Done, false, AccidentRegister.Status.Approved, false, false)
    }
    // 已登在审(登记状态为审核不通过)：15
    for (i in 61..75) {
      createData("${ymd}_$i", "car$i", "driver$i", scope, AccidentDraft.Status.Done, false, AccidentRegister.Status.Rejected, false, false)
    }
    // 已登在审(登记状态为待审核)且逾期登记：15
    for (i in 76..90) {
      createData("${ymd}_$i", "car$i", "driver$i", scope, AccidentDraft.Status.Done, false, AccidentRegister.Status.ToCheck, true, false)
    }
  }

  private fun createData(code: String, carPlate: String, driverName: String, happenTime: OffsetDateTime,
                         draftStatus: AccidentDraft.Status, overdueDraft: Boolean,
                         registerStatus: AccidentRegister.Status? = null, overdueRegister: Boolean? = null,
                         outsideDriver: Boolean? = null): AccidentRegister? {
    em.persist(
      AccidentDraft(
        code = code, status = draftStatus, happenTime = happenTime, reportTime = happenTime.plusHours(1),
        overdue = overdueDraft, carPlate = carPlate, driverName = driverName, location = "", hitForm = "",
        hitType = "", source = "", authorName = "", authorId = "", describe = ""
      )
    )
    if (draftStatus == AccidentDraft.Status.Done && registerStatus != null &&
      overdueRegister != null && outsideDriver != null) {
      val accidentRegister =
        AccidentRegister(
          code = code, status = registerStatus, happenTime = happenTime, registerTime = happenTime.plusHours(2),
          overdueRegister = overdueRegister, carPlate = carPlate, partyDriverName = driverName,
          draftTime = happenTime.plusHours(1), outsideDriver = outsideDriver,
          gpsLongitude = BigDecimal(50), gpsLatitude = BigDecimal(50), gpsSpeed = 30,
          carId = 1, motorcadeName = "", dutyDriverId = 1, dutyDriverName = "", describe = "",
          hitForm = "", hitType = "", weather = "", light = "", drivingDirection = "", roadType = "",
          roadStructure = "", roadState = "", carCount = 2, peopleCount = 2, otherCount = 0, dealDepartment = "",
          dealWay = "", insuranceCompany = "", insuranceCode = "", locationLevel1 = "", locationLevel2 = "",
          locationLevel3 = "", locationOther = ""
        )
      em.persist(accidentRegister)
      return accidentRegister
    }
    return null
  }


  @Test
  fun findTodoWithDraftStatus() {
    // mock
    val driver = CarMan(
      id = 1, uid = "uid", status = CarMan.Status.Enabled, type = CarMan.Type.Driver, name = "Name",
      sex = CarMan.Sex.Male, origin = "", idCardNo = "", initialLicenseDate = LocalDate.now(), model = "",
      workDate = LocalDate.now(), serviceCertNo = "", phone = ""
    )
    val now = OffsetDateTime.now()
    // 构建事故报案，司机：非编，报案状态：未登记
    createData("01", "car1", "driver1", now, AccidentDraft.Status.Todo, false)
    // 构建事故报案，司机：在编，报案状态：未登记
    createData("02", "car2", driver.name, now, AccidentDraft.Status.Todo, false)
    // 构建事故报案，司机：非编，报案状态：已登记，登记状态：审核通过
    createData("03", "car3", "driver3", now, AccidentDraft.Status.Done, false, AccidentRegister.Status.Approved, false, true)
    em.persist(driver); em.flush(); em.clear()

    // invoke
    val actual = dao.findTodo(AccidentRegister.Status.Draft)

    // verify
    StepVerifier.create(actual)
      .consumeNextWith {
        assertEquals("01", it.code)
        assertEquals("driver1", it.driverName)
        assertEquals(now, it.happenTime)
        assertTrue(it.outsideDriver)
        assertNull(it.registerTime)
        assertNull(it.overdueRegister)
        assertNull(it.submitTime)
      }
      .consumeNextWith {
        assertEquals("02", it.code)
        assertEquals(driver.name, it.driverName)
        assertEquals(now, it.happenTime)
        assertFalse(it.outsideDriver)
        assertNull(it.registerTime)
        assertNull(it.overdueRegister)
        assertNull(it.submitTime)
      }
      .verifyComplete()
  }

  @Test
  fun findTodoWithToCheckStatus() {
    // mock
    val now = OffsetDateTime.now()
    // 构建事故报案，司机：非编，报案状态：已登记，登记状态：待审核，登记时间：事发时间两小时后，登记逾期：false
    createData("01", "car1", "driver1", now, AccidentDraft.Status.Done, false, AccidentRegister.Status.ToCheck, false, true)
    // 构建事故报案，司机：非编，报案状态：已登记，登记状态：待审核，登记时间：事发时间两小时后，登记逾期：false
    val accidentRegister =
      createData("02", "car2", "driver2", now, AccidentDraft.Status.Done, false, AccidentRegister.Status.ToCheck, false, true)
    // 构建事故报案，司机：非编，报案状态：已登记，登记状态：审核通过，登记时间：事发时间两小时后，登记逾期：false
    createData("03", "car3", "driver3", now, AccidentDraft.Status.Done, false, AccidentRegister.Status.Approved, false, true)
    // 构建事务操作记录，操作类型：提交审核，事务类型：事故登记，事故登记 ID：2，操作时间：事发时间一天后
    em.persist(
      AccidentOperation(
        operatorId = 1, operatorName = "name", operateTime = now.plusDays(1),
        operationType = AccidentOperation.OperationType.Confirmation, targetType = AccidentOperation.TargetType.Register,
        targetId = accidentRegister?.id!!, comment = null, attachmentId = null, attachmentName = null
      )
    )
    // 构建事务操作记录，操作类型：提交审核，事务类型：事故登记，事故登记 ID：2，操作时间：事发时间两天后
    em.persist(
      AccidentOperation(
        operatorId = 1, operatorName = "name", operateTime = now.plusDays(2),
        operationType = AccidentOperation.OperationType.Confirmation, targetType = AccidentOperation.TargetType.Register,
        targetId = accidentRegister.id!!, comment = null, attachmentId = null, attachmentName = null
      )
    )
    em.flush(); em.clear()

    // invoke
    val actual = dao.findTodo(AccidentRegister.Status.ToCheck)

    // verify
    StepVerifier.create(actual)
      .consumeNextWith {
        assertEquals("01", it.code)
        assertEquals("driver1", it.driverName)
        assertEquals(now, it.happenTime)
        assertEquals(now.plusHours(2), it.registerTime)
        assertTrue(it.outsideDriver)
        assertFalse(it.overdueRegister!!)
        assertNull(it.submitTime)
      }
      .consumeNextWith {
        assertEquals("02", it.code)
        assertEquals("driver2", it.driverName)
        assertEquals(now, it.happenTime)
        assertEquals(now.plusHours(2), it.registerTime)
        assertEquals(now.plusDays(2), it.submitTime)
        assertTrue(it.outsideDriver)
        assertFalse(it.overdueRegister!!)
      }
      .verifyComplete()
  }

  @Test
  fun findTodoWithNullStatus() {
    // mock
    val now = OffsetDateTime.now()
    // 构建事故报案，司机：非编，报案状态：未登记
    createData("01", "car1", "driver1", now, AccidentDraft.Status.Todo, false)
    // 构建事故报案，司机：非编，报案状态：已登记，登记状态：待审核，登记时间：事发时间两小时后，登记逾期：false
    createData("02", "car2", "driver2", now, AccidentDraft.Status.Done, false, AccidentRegister.Status.ToCheck, false, true)
    // 构建事故报案，司机：非编，报案状态：已登记，登记状态：审核通过
    createData("03", "car3", "driver3", now, AccidentDraft.Status.Done, false, AccidentRegister.Status.Approved, false, true)
    em.flush(); em.clear()

    // invoke
    val actual = dao.findTodo(null)

    // verify
    StepVerifier.create(actual)
      .consumeNextWith {
        assertEquals("01", it.code)
        assertEquals("driver1", it.driverName)
        assertEquals(now, it.happenTime)
        assertTrue(it.outsideDriver)
        assertNull(it.registerTime)
        assertNull(it.overdueRegister)
        assertNull(it.submitTime)
      }
      .consumeNextWith {
        assertEquals("02", it.code)
        assertEquals("driver2", it.driverName)
        assertEquals(now, it.happenTime)
        assertEquals(now.plusHours(2), it.registerTime)
        assertTrue(it.outsideDriver)
        assertFalse(it.overdueRegister!!)
        assertNull(it.submitTime)
      }
      .verifyComplete()

    assertThrows(IllegalArgumentException::class.java, { dao.findTodo(AccidentRegister.Status.Approved).subscribe() })
  }
}