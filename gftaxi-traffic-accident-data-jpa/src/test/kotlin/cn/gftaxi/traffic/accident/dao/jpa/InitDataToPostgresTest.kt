package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.jpa.InitDataToPostgresTest.Cfg
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentRegisterRecord4EachStatus
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentRegister
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
  @PersistenceContext private val em: EntityManager
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

    // 仅报案案件(未有登记信息) 1 宗
    em.persist(randomAccidentDraft(
      code = nextCode(baseTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentDraft.Status.Todo,
      happenTime = baseTime,
      overdueDraft = false
    ))

    // 各个状态的事故登记信息都初始化 1 条数据，共 4 条
    val records = randomAccidentRegisterRecord4EachStatus(
      em = em,
      baseTime = baseTime.plusHours(-1),
      positive = false
    )

    // 对审核不通过的那条增加多一次的提交和审核不通过记录（连续两次不通过）
    val register = records[AccidentRegister.Status.Rejected]!!.first
    var rejection = records[AccidentRegister.Status.Rejected]!!.third[OperationType.Rejection]!!
    // 1. 再次提交审核
    val confirmation2 = POUtils.randomAccidentOperation(
      operateTime = rejection.operateTime.plusHours(1),
      operationType = OperationType.Confirmation,
      targetId = register.id!!,
      targetType = TargetType.Register
    )
    em.persist(confirmation2)
    // 2. 再次审核不通过
    rejection = POUtils.randomAccidentOperation(
      operateTime = confirmation2.operateTime.plusHours(1),
      operationType = OperationType.Rejection,
      targetId = register.id!!,
      targetType = TargetType.Register
    )
    em.persist(rejection)
  }
}