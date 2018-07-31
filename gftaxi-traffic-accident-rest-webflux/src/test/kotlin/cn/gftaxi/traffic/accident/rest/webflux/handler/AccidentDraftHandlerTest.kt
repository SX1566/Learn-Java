package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.FIND_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.GET_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.SUBMIT_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler.Companion.UPDATE_REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import javax.json.Json

/**
 * 测试事故报案 Rest 接口的 [HandlerFunction]。
 *
 * @author cjw
 */
@SpringJUnitConfig(AccidentDraftHandler::class)
@MockBean(AccidentDraftService::class)
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
    list.add(AccidentDraft(null,
      code, AccidentDraft.Status.Todo, "car", "driver", OffsetDateTime.now(),
      OffsetDateTime.now(), "location", "hitForm", "hitType", false,
      "source", "authorName", "authorId", ""))
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
    val client = bindToRouterFunction(RouterFunctions.route(GET_REQUEST_PREDICATE, HandlerFunction(handler::get))).build()
    // mock
    val code = "20180709_01"
    `when`(accidentDraftService.get(code))
      .thenReturn(Mono.just(AccidentDraft(null,
        code, AccidentDraft.Status.Todo, "car", "driver", OffsetDateTime.now(),
        OffsetDateTime.now(), "location", "hitForm", "hitType", false,
        "source", "authorName", "authorId", "")))

    // invoke
    client.get().uri("/accident-draft/$code")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.code").isEqualTo(code) // verify code

    // verify
    verify(accidentDraftService).get(code)
  }

  @Test
  fun submit() {
    val client = bindToRouterFunction(RouterFunctions.route(SUBMIT_REQUEST_PREDICATE, HandlerFunction(handler::submit))).build()
    // mock
    val code = "20180909_01"
    val now = OffsetDateTime.now()
    val dto = AccidentDraftDto4Submit("粤A.23J5", "林河", now, "荔湾区福利路",
      "车辆间事故", "追尾碰撞", "撞车", "BC", "韩智勇",
      "hzy", now)
    val data = Json.createObjectBuilder()
    `data`.add("carPlate", dto.carPlate)
    `data`.add("driverName", dto.driverName)
    `data`.add("happenTime", dto.happenTime.format(FORMAT_DATE_TIME_TO_MINUTE))
    `data`.add("location", dto.location)
    `data`.add("hitForm", dto.hitForm)
    `data`.add("hitType", dto.hitType)
    `data`.add("describe", dto.describe)
    `data`.add("source", dto.source)
    `data`.add("authorName", dto.authorName)
    `data`.add("authorId", dto.authorId)
    `data`.add("reportTime", dto.reportTime.format(FORMAT_DATE_TIME_TO_MINUTE))

    `when`(accidentDraftService.submit(any())).thenReturn(Mono.just(code))

    // invoke
    client.post().uri("/accident-draft")
      .header("Content-Type", APPLICATION_JSON_UTF8.toString())
      .syncBody(data.build().toString())
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.code").isEqualTo(code) // verify code

    // verify
    verify(accidentDraftService).submit(any())
  }

  @Test
  fun update() {
    val client = bindToRouterFunction(RouterFunctions.route(UPDATE_REQUEST_PREDICATE, HandlerFunction(handler::update))).build()
    // mock
    val code = "code"
    val happenTimeOfString = LocalDateTime.now().format(FORMAT_DATE_TIME_TO_MINUTE)
    val happenTime = OffsetDateTime.of(LocalDateTime.parse(happenTimeOfString, FORMAT_DATE_TIME_TO_MINUTE), OffsetDateTime.now().offset)
    val dto = AccidentDraftDto4Modify("carPlate", "driver", happenTime, "location", "hitForm", "hitType", "describe")
    val data = Json.createObjectBuilder()
    data.add("carPlate", dto.carPlate)
    data.add("driverName", dto.driverName)
    data.add("happenTime", happenTimeOfString)
    data.add("location", dto.location)
    data.add("hitForm", dto.hitForm)
    data.add("hitType", dto.hitType)
    data.add("describe", dto.describe)
    `when`(accidentDraftService.modify(anyString(), any())).thenReturn(Mono.empty())

    // invoke
    client.put().uri("/accident-draft/$code")
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()
      .expectStatus().isNoContent

    // verify
    verify(accidentDraftService).modify(anyString(), any())
  }
}