package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextCode
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.nextId
import cn.gftaxi.traffic.accident.po.AccidentOperation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import tech.simter.reactive.context.SystemContext.User
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentOperationDaoImpl].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class AccidentOperationDaoImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentOperationDao
) {
  @Test
  fun create() {
    // mock
    val now = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val expected = AccidentOperation(
      operationType = AccidentOperation.OperationType.Confirmation,
      targetType = AccidentOperation.TargetType.Register,
      targetId = nextId(AccidentOperation::class.simpleName!!),
      operateTime = now,
      operatorId = nextId(User::class.simpleName!!),
      operatorName = nextCode(User::class.simpleName!!)
    )
    assertNull(expected.id)

    // invoke
    StepVerifier.create(dao.create(expected))
      .consumeNextWith { assertNotNull(it.id) }
      .verifyComplete()

    // verify
    assertEquals(expected, em
      .createQuery("select a from AccidentOperation a where id = :id", AccidentOperation::class.java)
      .setParameter("id", expected.id)
      .singleResult)
  }
}