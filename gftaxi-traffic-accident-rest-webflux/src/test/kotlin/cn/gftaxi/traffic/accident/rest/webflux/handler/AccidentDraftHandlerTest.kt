package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.Utils
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.FIND_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.GET_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.SUBMIT_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.UPDATE_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import java.time.OffsetDateTime
import java.util.*
import javax.json.Json

/**
 * 测试事故报案 Rest 接口的 [HandlerFunction]。
 *
 * @author cjw
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, AccidentDraftHandler::class)
@MockBean(AccidentDraftService::class)
@WebFluxTest
class AccidentDraftHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentDraftService: AccidentDraftService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theGetRoute(handler: AccidentDraftHandler): RouterFunction<ServerResponse> =
      route(FIND_REQUEST_PREDICATE, HandlerFunction { handler.find(it) })
        .andRoute(GET_REQUEST_PREDICATE, HandlerFunction { handler.get(it) })
        .andRoute(SUBMIT_REQUEST_PREDICATE, HandlerFunction { handler.submit(it) })
        .andRoute(UPDATE_REQUEST_PREDICATE, HandlerFunction { handler.update(it) })
  }

  private fun randomAccidentDraft(id: Int? = null, code: String): AccidentDraft {
    return AccidentDraft(
      id = id,
      code = code, status = AccidentDraft.Status.Todo, carPlate = "car", driverName = "driver",
      happenTime = OffsetDateTime.now(), createTime = OffsetDateTime.now(), location = "location",
      hitForm = "hitForm", hitType = "hitType", overdueCreate = false,
      source = "source", authorName = "authorName", authorId = "authorId"
    )
  }

  @Test
  fun find() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val search = "2018"
    val status = AccidentDraft.Status.Todo
    val code = "20180709_01"
    val list = ArrayList<AccidentDraft>()
    list.add(randomAccidentDraft(code = code))
    `when`(accidentDraftService.find(pageNo, pageSize, status, search))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())))

    // invoke
    client.get().uri("/accident-draft?pageNo=$pageNo&pageSize=$pageSize&status=$status&search=$search")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size) // verify count
      .jsonPath("$.pageNo").isEqualTo(pageNo)     // verify pageNo
      .jsonPath("$.pageSize").isEqualTo(pageSize) // verify pageSize
      .jsonPath("$.rows[0].code").isEqualTo(code)    // verify AccidentDraft.code

    // verify
    verify(accidentDraftService).find(pageNo, pageSize, status, search)
  }

  @Test
  fun get() {
    // mock
    val id = 1
    val code = "20180709_01"
    `when`(accidentDraftService.get(id))
      .thenReturn(Mono.just(randomAccidentDraft(id = id, code = code)))

    // invoke
    client.get().uri("/accident-draft/$id")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.id").isEqualTo(id)
      .jsonPath("$.code").isEqualTo(code)

    // verify
    verify(accidentDraftService).get(id)
  }

  @Test
  fun submit() {
    // mock
    val now = OffsetDateTime.now()
    val dto = AccidentDraftDto4Submit().apply {
      carPlate = "粤A.23J5"
      driverName = "林河"
      happenTime = now
      location = "荔湾区福利路"
      hitForm = "车辆间事故"
      hitType = "追尾碰撞"
      describe = "撞车"
      source = "BC"
      authorName = "韩智勇"
      authorId = "hzy"
      createTime = now
    }
    val data = Json.createObjectBuilder()
    `data`.add("carPlate", dto.carPlate)
    `data`.add("driverName", dto.driverName)
    `data`.add("happenTime", dto.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
    `data`.add("location", dto.location)
    `data`.add("hitForm", dto.hitForm)
    `data`.add("hitType", dto.hitType)
    `data`.add("describe", dto.describe)
    `data`.add("source", dto.source)
    `data`.add("authorName", dto.authorName)
    `data`.add("authorId", dto.authorId)
    `data`.add("createTime", dto.createTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))

    val id = 1
    val code = "20180909_01"
    `when`(accidentDraftService.submit(any())).thenReturn(Mono.just(Pair(id, code)))

    // invoke
    client.post().uri("/accident-draft")
      .header("Content-Type", APPLICATION_JSON_UTF8.toString())
      .syncBody(data.build().toString())
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.id").isEqualTo(id)
      .jsonPath("$.code").isEqualTo(code)

    // verify
    verify(accidentDraftService).submit(any())
  }

  @Test
  fun updateBySuccess() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("carPlate", "粤A.N3402").add("driverName", "driver")
    `when`(accidentDraftService.modify(any(), any())).thenReturn(Mono.empty())

    // invoke
    val response = client.patch().uri("/accident-draft/$id")
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNoContent.expectBody().isEmpty
    verify(accidentDraftService).modify(any(), any())
  }

  @Test
  fun updateByNotFound() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("carPlate", "粤A.N3402").add("driverName", "driver")
    `when`(accidentDraftService.modify(any(), any())).thenReturn(Mono.error(NotFoundException("指定的案件不存在")))

    // invoke
    val response = client.patch().uri("/accident-draft/$id")
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNotFound.expectHeader().contentType(Utils.TEXT_PLAIN_UTF8)
    verify(accidentDraftService).modify(any(), any())
  }
}