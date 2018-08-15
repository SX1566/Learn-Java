package cn.gftaxi.traffic.accident.dao.jpa.draft

import cn.gftaxi.traffic.accident.Utils
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDraftDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dao.jpa.POUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Done
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Test [AccidentDraftDaoImpl.find].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class FindMethodImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
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

    // 1. 查找全部
    StepVerifier.create(dao.find())
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(poList.size.toLong(), page.totalElements)
        // 验证排序：状态正序+事发时间逆序
        assertEquals(poList.size, page.content.size)
        assertEquals(page.content[0], poList[1])
        assertEquals(page.content[1], poList[0])
        assertEquals(page.content[2], poList[2])
      }.verifyComplete()

    // 2. 仅查找 Done 状态
    StepVerifier.create(dao.find(status = Done))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(1L, page.totalElements)
        assertEquals(1, page.content.size)
        assertEquals(page.content[0], poList[2])
      }.verifyComplete()

    // 3. 仅查找待登记状态
    StepVerifier.create(dao.find(status = Todo))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(2L, page.totalElements)
        assertEquals(2, page.content.size)
        assertEquals(page.content[0], poList[1])
        assertEquals(page.content[1], poList[0])
      }.verifyComplete()

    // 4. 模糊搜索指定编号的待登记：不存在
    StepVerifier.create(dao.find(status = Todo, fuzzySearch = "${ymd}_0$i"))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(0L, page.totalElements)
        assertEquals(0, page.content.size)
      }.verifyComplete()

    // 5. 模糊搜索指定编号的待登记：存在
    StepVerifier.create(dao.find(status = Todo, fuzzySearch = "${ymd}_0${i - 1}"))
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(1L, page.totalElements)
        assertEquals(1, page.content.size)
        assertEquals(page.content[0], poList[1])
      }.verifyComplete()
  }
}