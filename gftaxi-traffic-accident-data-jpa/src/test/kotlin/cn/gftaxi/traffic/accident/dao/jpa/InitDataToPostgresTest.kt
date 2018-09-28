package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dao.jpa.InitDataToPostgresTest.Cfg
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.sql.DataSource

/**
 * 此类仅用于构建初始化数据到 postgres 数据库，方便 UI 界面上可以看到一些实际的数据。
 *
 * 请先将本类的 @Disabled 注释掉，然后在命令行执行如下语句：（数据库连接配置请修改 [Cfg.dataSource] 方法内的代码）
 *
 * ```
 * mvn test -P test,postgres -D test=cn.gftaxi.traffic.accident.dao.jpa.InitDataToPostgresTest
 * ```
 * 初始化数据只可以执行一次，多次执行会报唯一性约束失败的。
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Disabled
class InitDataToPostgresTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository
) {
  // 配置 postgres 的数据库连接配置
  @Configuration
  class Cfg {
    @Bean
    @Primary
    fun dataSource(): DataSource {
      val dataSource = DriverManagerDataSource()
      dataSource.setDriverClassName("org.postgresql.Driver")
      dataSource.url = "jdbc:postgresql://localhost:5432/test_accident"
      dataSource.username = "test"
      dataSource.password = "password"
      return dataSource
    }
  }

  @Test
  @Rollback(false)
  fun initData() {
    val baseTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)

    // 已报案待登记 1 宗
    createDraftStage(baseTime)

    // 各个状态的事故登记信息都初始化 1 条数据，共 4 条
    createRegisterStage(baseTime)
  }

  // 已报案待登记 1 宗
  private fun createDraftStage(baseTime: OffsetDateTime) {
    val pair = randomCase(
      id = null,
      happenTime = baseTime,
      stage = CaseStage.Drafting,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafting,
      registerStatus = AuditStatus.ToSubmit
    )
    caseRepository.save(pair.first)
    assertNotNull(pair.first.id)
    situationRepository.save(pair.second.apply { id = pair.first.id })
  }

  // 各个状态的事故登记信息都初始化 1 条数据，共 4 条
  private fun createRegisterStage(baseTime: OffsetDateTime) {
    var d = 1L
    // 待审核
    var pair = randomCase(
      id = null,
      happenTime = baseTime.minusDays(++d),
      stage = CaseStage.Registering,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafted,
      overdueRegister = false,
      registerStatus = AuditStatus.ToCheck
    )
    caseRepository.save(pair.first)
    assertNotNull(pair.first.id)
    situationRepository.save(pair.second.apply { id = pair.first.id })

    // 审核不通过
    pair = randomCase(
      id = null,
      happenTime = baseTime.minusDays(++d),
      stage = CaseStage.Registering,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafted,
      overdueRegister = false,
      registerStatus = AuditStatus.Rejected
    )
    caseRepository.save(pair.first)
    assertNotNull(pair.first.id)
    situationRepository.save(pair.second.apply { id = pair.first.id })

    // 审核通过
    pair = randomCase(
      id = null,
      happenTime = baseTime.minusDays(++d),
      stage = CaseStage.Registering,
      overdueDraft = false,
      draftStatus = DraftStatus.Drafted,
      overdueRegister = false,
      registerStatus = AuditStatus.Approved
    )
    caseRepository.save(pair.first)
    assertNotNull(pair.first.id)
    situationRepository.save(pair.second.apply { id = pair.first.id })

    // 对审核不通过的那条增加多一次的提交和审核不通过记录（连续两次不通过）TODO
  }
}