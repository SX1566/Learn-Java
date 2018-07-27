package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

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
}