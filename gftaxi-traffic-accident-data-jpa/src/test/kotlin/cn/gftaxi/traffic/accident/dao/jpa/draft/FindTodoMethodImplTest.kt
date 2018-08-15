package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.Utils
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDraftDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Done
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentDraftDaoImpl.findTodo].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class FindTodoMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
  @Test
  fun foundNothing() {
    StepVerifier.create(dao.findTodo()).verifyComplete()
  }

  @Test
  fun foundSomething() {
    // init data
    val now = OffsetDateTime.now().withHour(1)
    val ymd = now.format(Utils.FORMAT_TO_YYYYMMDD)
    var i = 0
    var j: Long = 0
    val poList = listOf(
      randomAccidentDraft(
        code = "${ymd}_0${++i}",
        status = Todo,
        happenTime = now.plusHours(++j)
      ),
      randomAccidentDraft(
        code = "${ymd}_0${++i}",
        status = Todo,
        happenTime = now.plusHours(++j)
      ),
      randomAccidentDraft(
        code = "${ymd}_0${++i}",
        status = Done,
        happenTime = now.plusHours(++j)
      )
    )
    em.run {
      poList.forEach { persist(it) }
      flush()
      clear()
    }

    // invoke
    val actual = dao.findTodo()

    // verify
    StepVerifier.create(actual)
      .expectNext(poList[1])
      .expectNext(poList[0])
      .verifyComplete()
  }
}