package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.AuditStatus.*
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMM
import cn.gftaxi.traffic.accident.dao.AccidentStatDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentStatDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.test.TestUtils.randomBoolean
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import cn.gftaxi.traffic.accident.test.TestUtils.randomOffsetDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.time.YearMonth

/**
 * Test [AccidentStatDaoImpl.statRegisterMonthlySummary].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
class StatMonthlySummaryMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentStatDao
) {
  @Test
  fun `Found nothing`() {
    val now = YearMonth.now()
    StepVerifier
      .create(dao.statRegisterMonthlySummary(now.minusMonths(11), now))
      .verifyComplete()
  }

  /**
   * 1. 随机生成若干条数据：时间范围在上年本月到今年本月，共13个月
   * 2. 并返回统计结果 [AccidentRegisterDto4StatSummary] 的 [List]，统计结果按月份逆序排序
   */
  private fun initData(): List<AccidentRegisterDto4StatSummary> {
    val offset = OffsetDateTime.now().offset
    val time = YearMonth.now().atDay(1).atStartOfDay().atOffset(offset)
    val statuses = AuditStatus.values()
    return (0..12).map { i ->
      // 对本月份随机生成若干条数据并计算出统计结果
      val start = time.minusMonths(i.toLong())
      val end = start.plusMonths(1)
      (0..randomInt(0, 9)).map {
        randomCase(
          id = null,
          happenTime = randomOffsetDateTime(start, end),
          overdueDraft = randomBoolean(),
          overdueRegister = randomBoolean(),
          registerStatus = statuses[randomInt(0, statuses.size - 1)]
        )
          .apply {
            caseRepository.saveAndFlush(first)
            situationRepository.saveAndFlush(second.apply { id = first.id })
          }
      }.run {
        AccidentRegisterDto4StatSummary(
          scope = start.format(FORMAT_TO_YYYYMM),
          total = size,
          checked = count { it.second.registerStatus == Approved },
          checking = count { it.second.registerStatus.let { status -> status == ToCheck || status == Rejected } },
          drafting = count { it.second.registerStatus == ToSubmit },
          overdueDraft = count { it.second.overdueDraft == true },
          overdueRegister = count { it.second.overdueRegister == true }
        )
      }
    }
  }

  @Test
  fun `Found something`() {
    val now = YearMonth.now()
    val data = initData()
    StepVerifier.create(dao.statRegisterMonthlySummary(now.minusMonths(11), now).collectList())
      .consumeNextWith {
        // 统计往期12个月的数据，数据库有往期13个月的数据
        assertEquals(data.subList(0, 12), it)
      }
      .verifyComplete()
  }
}