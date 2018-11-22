package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.AuditStatus.*
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.time.Year
import java.time.YearMonth

/**
 * Test [AccidentStatDaoImpl.statRegisterYearlySummary].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
class StatYearlySummaryMethodImplTest @Autowired constructor(
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val dao: AccidentStatDao
) {
  @Test
  fun `Found nothing`() {
    val now = Year.now()
    StepVerifier
      .create(dao.statRegisterYearlySummary(now.minusYears(1), now))
      .verifyComplete()
  }

  /**
   * 1. 随机生成若干条数据：时间范围在前年到今年本月，共3年
   * 2. 并返回统计结果 [AccidentRegisterDto4StatSummary] 的 [List]，统计结果按年份逆序排序
   */
  private fun initData(): List<AccidentRegisterDto4StatSummary> {
    val offset = OffsetDateTime.now().offset
    val time = YearMonth.now().atDay(1).atStartOfDay().atOffset(offset)
    val statuses = AuditStatus.values()
    return (0..2).map { i ->
      // 对本年随机生成若干条数据并计算出统计结果
      val start = time.minusYears(i.toLong())
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
          scope = start.year.toString(),
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
    val now = Year.now()
    val data = initData()
    StepVerifier.create(dao.statRegisterYearlySummary(now.minusYears(1), now).collectList())
      .consumeNextWith {
        // 统计往期2年的数据，数据库有往期3年的数据
        Assertions.assertEquals(data.subList(0, 2), it)
      }
      .verifyComplete()
  }
}