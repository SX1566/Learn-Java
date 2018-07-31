package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegister
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.Month
import java.time.OffsetDateTime
import java.time.Year
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentRegisterDao.statSummary].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class StatSummaryMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentRegisterDao
) {
  private val logger = LoggerFactory.getLogger(StatSummaryMethodImplTest::class.java)

  @Test
  fun test() {
    // mock
    val now = OffsetDateTime.now()
    // 构建本月数据
    val expectedCurrentMonthStat = initYearMonthData(Year.of(now.year), now.month)
    // 构建上月数据
    val expectedLastMonthStat = initYearMonthData(Year.of(now.year), now.month.minus(1))
    // 本年数据 = 本月 + 上月
    val expectedCurrentYearStat = AccidentRegisterDto4StatSummary(
      scope = "本年",
      total = expectedCurrentMonthStat.total + expectedLastMonthStat.total,
      checked = expectedCurrentMonthStat.checked + expectedLastMonthStat.checked,
      checking = expectedCurrentMonthStat.checking + expectedLastMonthStat.checking,
      drafting = expectedCurrentMonthStat.drafting + expectedLastMonthStat.drafting,
      overdueDraft = expectedCurrentMonthStat.overdueDraft + expectedLastMonthStat.overdueDraft,
      overdueRegister = expectedCurrentMonthStat.overdueRegister + expectedLastMonthStat.overdueRegister
    )
    em.flush()
    em.clear()

    // invoke
    val actual = dao.statSummary()

    // verify
    StepVerifier.create(actual)
      .expectNext(expectedCurrentMonthStat)
      .expectNext(expectedLastMonthStat)
      .expectNext(expectedCurrentYearStat)
      .verifyComplete()
  }

  // 构建指定月份的事故报案、事故登记信息
  private fun initYearMonthData(year: Year, month: Month): AccidentRegisterDto4StatSummary {
    logger.debug("==============year=${year.value}, month=${month.value}")
    val now = OffsetDateTime.now()
    val ymTime = OffsetDateTime.of(year.value, month.value, 1, now.hour, now.minute, 0, 0, now.offset)
    val ymd = ymTime.format(FORMAT_TO_YYYYMMDD)
    var total = 0
    var checked = 0
    var checking = 0
    var drafting = 0
    var overdueDraft = 0
    var overdueRegister = 0

    // 1. 尚未登记 5 宗，其中逾期 2 宗
    // 1.1. 尚未登记 3 宗（未有登记信息），其中逾期报案 1 宗
    total += 3
    drafting += 3
    overdueDraft += 1
    for (i in 3..5) {
      logger.debug("==============1.1:$i 尚未登记 3 宗（未有登记信息），其中逾期报案 1 宗")
      em.persist(randomAccidentDraft(
        code = nextCode(ymd),
        status = AccidentDraft.Status.Todo,
        happenTime = when {
          i % 3 == 0 -> ymTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS) // 月头（方便检测 SQL 的边界条件）
          i % 3 == 1 -> ymTime.withDayOfMonth(15) // 月中
          else -> ymTime.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).minusSeconds(1) // 月尾（方便检测 SQL 的边界条件）
        },
        overdue = (i % 3 == 0)
      ))
    }

    // 1.2. 尚未登记 2 宗（登记信息处于草稿状态），其中逾期报案 1 宗
    total += 2
    drafting += 2
    overdueDraft += 1
    for (i in 2..3) {
      logger.debug("==============1.2:$i 尚未登记 2 宗（登记信息处于草稿状态），其中逾期报案 1 宗")
      val accidentDraft = randomAccidentDraft(
        code = nextCode(ymd),
        status = AccidentDraft.Status.Todo,
        happenTime = ymTime.plusMinutes(i.toLong()),
        overdue = (i % 2 == 0)
      )
      em.persist(accidentDraft)
      em.persist(randomAccidentRegister(
        draft = accidentDraft,
        status = AccidentRegister.Status.Draft,
        driverType = AccidentRegister.DriverType.Official,
        overdue = null
      ))
    }

    // 2. 已登已审案件 4 宗，其中：
    //    (正常报案+正常登记) 1 宗
    //    (正常报案+逾期登记) 1 宗
    //    (逾期报案+正常登记) 1 宗
    //    (逾期报案+逾期登记) 1 宗
    total += 4
    checked += 4
    overdueDraft += 2
    overdueRegister += 2
    for (i in 4..7) {
      logger.debug("==============2:$i 已登已审案件 4 宗，其中逾期报案、逾期登记各 2 宗")
      createRegisterWithDoneDraft(
        code = nextCode(ymd),
        index = i,
        baseTime = ymTime,
        status = AccidentRegister.Status.Approved
      )
    }

    // 3. 已登在审案件(登记信息状态为待审核) 4 宗，其中：
    //    (正常报案+正常登记) 1 宗
    //    (正常报案+逾期登记) 1 宗
    //    (逾期报案+正常登记) 1 宗
    //    (逾期报案+逾期登记) 1 宗
    total += 4
    checking += 4
    overdueDraft += 2
    overdueRegister += 2
    for (i in 4..7) {
      logger.debug("==============3:$i 已登在审案件(登记信息状态为待审核) 4 宗，其中逾期报案、逾期登记各 2 宗")
      createRegisterWithDoneDraft(
        code = nextCode(ymd),
        index = i,
        baseTime = ymTime,
        status = AccidentRegister.Status.ToCheck
      )
    }

    // 4. 已登在审案件(登记信息状态为审核不通过) 4 宗，其中：
    //    (正常报案+正常登记) 1 宗
    //    (正常报案+逾期登记) 1 宗
    //    (逾期报案+正常登记) 1 宗
    //    (逾期报案+逾期登记) 1 宗
    total += 4
    checking += 4
    overdueDraft += 2
    overdueRegister += 2
    for (i in 4..7) {
      logger.debug("==============3:$i 已登在审案件(登记信息状态为审核不通过) 4 宗，其中逾期报案、逾期登记各 2 宗")
      createRegisterWithDoneDraft(
        code = nextCode(ymd),
        index = i,
        baseTime = ymTime,
        status = AccidentRegister.Status.Rejected
      )
    }

    return AccidentRegisterDto4StatSummary(
      scope = when {
        year.value == now.year && month == now.month -> "本月"
        year.value == now.year && month == now.month.minus(1) -> "上月"
        else -> throw IllegalArgumentException("不支持的年月参数")
      },
      total = total,
      checked = checked,
      checking = checking,
      drafting = drafting,
      overdueDraft = overdueDraft,
      overdueRegister = overdueRegister
    )
  }

  private fun createRegisterWithDoneDraft(code: String, index: Int, baseTime: OffsetDateTime, status: AccidentRegister.Status) {
    val accidentDraft = randomAccidentDraft(
      code = code,
      status = AccidentDraft.Status.Done,
      happenTime = when {
        index % 4 == 0 -> baseTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS) // 月头（方便检测 SQL 的边界条件）
        index % 4 == 1 -> baseTime.withDayOfMonth(15) // 月中
        index % 4 == 2 -> baseTime.withDayOfMonth(25) // 月中+10d
        else -> baseTime.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).minusSeconds(1) // 月尾（方便检测 SQL 的边界条件）
      },
      overdue = when {
        index % 4 == 0 -> false
        index % 4 == 1 -> false
        index % 4 == 2 -> true
        else -> true
      }
    )
    em.persist(accidentDraft)
    em.persist(randomAccidentRegister(
      draft = accidentDraft,
      status = status,
      driverType = AccidentRegister.DriverType.Official,
      overdue = when {
        index % 4 == 0 -> false
        index % 4 == 1 -> true
        index % 4 == 2 -> false
        else -> true
      }
    ))
  }
}