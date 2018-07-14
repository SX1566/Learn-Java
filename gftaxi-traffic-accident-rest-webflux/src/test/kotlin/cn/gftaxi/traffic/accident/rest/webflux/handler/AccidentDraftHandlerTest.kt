package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.rest.webflux.ModuleConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.FIND_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.GET_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentCategoryService
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.*

/**
 * 测试事故报案 Rest 接口的 [HandlerFunction]。
 *
 * @author cjw
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@MockBean(AccidentDraftService::class, AccidentCategoryService::class)
class AccidentDraftHandlerTest @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val handler: AccidentDraftHandler
) {

  @Test
  fun find() {
    val client = bindToRouterFunction(RouterFunctions.route(FIND_REQUEST_PREDICATE, HandlerFunction(handler::find))).build()
    // mock
    val pageNo = 1
    val pageSize = 25
    val search = "2018"
    val status = AccidentDraft.Status.Todo
    val code = "20180709_01"
    val list = ArrayList<AccidentDraft>()
    list.add(AccidentDraft(
      code, AccidentDraft.Status.Todo, "car", "driver", OffsetDateTime.now(),
      OffsetDateTime.now(), "location", "hitForm", "hitType", false,
      "source", "authorName", "authorId", ""))
    `when`(accidentDraftService.find(pageNo.minus(1), pageSize, status, search))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())))

    // invoke
    client.get().uri("/accident-draft?pageNo=$pageNo&pageSize=$pageSize&status=$status&search=$search")
      .header("Content-Type", APPLICATION_JSON_UTF8.toString())
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size) // verify count
      .jsonPath("$.pageNo").isEqualTo(pageNo)     // verify pageNo
      .jsonPath("$.pageSize").isEqualTo(pageSize) // verify pageSize
      .jsonPath("$.rows[0].code").isEqualTo(code)    // verify AccidentDraft.code

    // verify
    verify(accidentDraftService).find(pageNo.minus(1), pageSize, status, search)
  }

  @Test
  fun get() {
    val client = bindToRouterFunction(RouterFunctions.route(GET_REQUEST_PREDICATE, HandlerFunction(handler::get))).build()
    // mock
    val code = "20180709_01"
    `when`(accidentDraftService.get(code))
      .thenReturn(Mono.just(AccidentDraft(
        code, AccidentDraft.Status.Todo, "car", "driver", OffsetDateTime.now(),
        OffsetDateTime.now(), "location", "hitForm", "hitType", false,
        "source", "authorName", "authorId", "")))

    // invoke
    client.get().uri("/accident-draft/$code")
      .header("Content-Type", APPLICATION_JSON_UTF8.toString())
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.code").isEqualTo(code) // verify code

    // verify
    verify(accidentDraftService).get(code)
  }
}