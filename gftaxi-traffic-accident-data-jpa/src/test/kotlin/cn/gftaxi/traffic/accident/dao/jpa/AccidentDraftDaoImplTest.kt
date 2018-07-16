package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.test.assertEquals

/**
 * 测试事故报案 Dao 实现。
 *
 * @author JF
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@DataJpaTest
class AccidentDraftDaoImplTest @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val dao: AccidentDraftDao
) {
  @Test
  fun find() {
    // mock
    val now = OffsetDateTime.now()
    val po1 = AccidentDraft("20180713_01", Status.Done, "search", "driver1", now, now, "", "", "", true, "", "", "", "")
    val po2 = AccidentDraft("20180713_02", Status.Done, "plate1", "search", now, now.plusHours(1), "", "", "", true, "", "", "", "")
    val po3 = AccidentDraft("20180713_03", Status.Todo, "plate2", "driver2", now, now.plusHours(2), "", "", "", true, "", "", "", "")
    val po4 = AccidentDraft("search", Status.Done, "plate3", "driver3", now, now.plusHours(3), "", "", "", true, "", "", "", "")
    em.persist(po1); em.persist(po2); em.persist(po3); em.persist(po4)
    em.flush(); em.clear()

    // invoke
    val actual4DefaultOrderBy = dao.find(1, 25, null, null)
    val actualWithStatus = dao.find(1, 25, AccidentDraft.Status.Done, null)
    val actualWithSearch = dao.find(1, 25, null, "sear")

    // verify
    StepVerifier.create(actual4DefaultOrderBy)
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(4L, page.totalElements)
        // 验证排序：报案时间逆序排序
        assertEquals(po4, page.content[0])
        assertEquals(po3, page.content[1])
        assertEquals(po2, page.content[2])
        assertEquals(po1, page.content[3])
      }
      .verifyComplete()
    StepVerifier.create(actualWithStatus)
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(3L, page.totalElements)
        assertEquals(po4, page.content[0])
        assertEquals(po2, page.content[1])
        assertEquals(po1, page.content[2])
      }
      .verifyComplete()
    StepVerifier.create(actualWithSearch)
      .consumeNextWith { page ->
        assertEquals(0, page.number)
        assertEquals(25, page.size)
        assertEquals(3L, page.totalElements)
        assertEquals(po4, page.content[0])
        assertEquals(po2, page.content[1])
        assertEquals(po1, page.content[2])
      }
      .verifyComplete()
  }

  @Test
  fun findTodo() {
    // mock
    val now = OffsetDateTime.now()
    val po1 = AccidentDraft("20180713_01", Status.Done, "car1", "a", now, now, "", "", "", true, "", "", "", "")
    val po2 = AccidentDraft("20180713_02", Status.Todo, "car2", "a", now, now.plusHours(1), "", "", "", true, "", "", "", "")
    val po3 = AccidentDraft("20180713_03", Status.Todo, "car3", "a", now, now.plusHours(2), "", "", "", true, "", "", "", "")
    em.persist(po1); em.persist(po2); em.persist(po3)
    em.flush(); em.clear()

    // invoke
    val actual = dao.findTodo()

    // verify
    StepVerifier.create(actual)
      .expectNext(po3)
      .expectNext(po2)
      .verifyComplete()
  }

  @Test
  fun get() {
    // mock
    val now = OffsetDateTime.now()
    val po1 = AccidentDraft("20180713_01", Status.Done, "search", "driver", now, now, "", "", "", true, "", "", "", "")
    em.persist(po1); em.flush(); em.clear()

    // invoke
    val actual = dao.get(po1.code)

    // verify
    StepVerifier.create(actual).expectNext(po1).verifyComplete()
  }

  @Test
  fun create() {
    // mock
    val now = OffsetDateTime.now()
    val po = AccidentDraft("20180713_01", Status.Done, "search", "driver", now, now, "", "", "", true, "", "", "", "")
    val expected = po.copy(happenTime = po.happenTime.truncatedTo(ChronoUnit.MINUTES))

    // invoke
    StepVerifier.create(dao.create(po)).expectNext().verifyComplete()

    // verify
    assertEquals(expected, em.createQuery("select a from AccidentDraft a", AccidentDraft::class.java).singleResult)
    assertThrows(IllegalArgumentException::class.java, { dao.create(po).block() })
  }

  @Test
  fun update() {
    // mock
    val now = OffsetDateTime.now()
    val code = "20180713_01"
    val po = AccidentDraft(code, Status.Done, "a", "a", now, now, "a", "a", "a", true, "a", "a", "a", "a")
    em.persist(po); em.flush(); em.clear()
    val data = mapOf(
      "carPlate" to "nPlate", "driverName" to "nDriver", "happenTime" to now.plusHours(1), "describe" to "nDescribe",
      "reportTime" to now.plusHours(1), "location" to "nLocation", "authorId" to "nAuthor"
    )

    // invoke
    StepVerifier.create(dao.update(code, data)).expectNext(true).verifyComplete()

    // verify
    val actual = em.createQuery("select a from AccidentDraft a", AccidentDraft::class.java).singleResult
    assertEquals(data["carPlate"], actual.carPlate)
    assertEquals(data["driverName"], actual.driverName)
    assertEquals((data["happenTime"] as OffsetDateTime).truncatedTo(ChronoUnit.MINUTES), actual.happenTime)
    assertEquals(data["describe"], actual.describe)
    assertEquals(data["reportTime"], actual.reportTime)
    assertEquals(data["location"], actual.location)
    assertEquals(data["authorId"], actual.authorId)

    StepVerifier.create(dao.update(code, mapOf())).expectNext(true).verifyComplete()
  }

  @Test
  fun nextCode() {
    // mock
    val now = OffsetDateTime.now()
    val ymd = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    // invoke and verify
    StepVerifier.create(dao.nextCode(now)).expectNext("${ymd}_01").verifyComplete()

    // mock
    em.persist(AccidentDraft("${ymd}_01", Status.Done, "plate1", "driver", now, now, "", "", "", true, "", "", "", ""))
    em.flush(); em.clear()

    // invoke and verify
    StepVerifier.create(dao.nextCode(now)).expectNext("${ymd}_02").verifyComplete()

    // mock
    em.persist(AccidentDraft("${ymd}_10", Status.Done, "plate2", "driver", now, now, "", "", "", true, "", "", "", ""))
    em.flush(); em.clear()

    // invoke and verify
    StepVerifier.create(dao.nextCode(now)).expectNext("${ymd}_11").verifyComplete()
  }
}