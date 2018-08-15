package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.ScopeType
import cn.gftaxi.traffic.accident.rest.webflux.Utils
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatSummaryHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import tech.simter.exception.PermissionDeniedException
import java.util.*
import java.util.stream.IntStream
import kotlin.collections.ArrayList

fun random(start: Int, end: Int) = Random().nextInt(end + 1 - start) + start

/**
 * Test [StatSummaryHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(StatSummaryHandler::class)
@EnableWebFlux
@MockBean(AccidentRegisterService::class)
class StatSummaryHandlerTest @Autowired constructor(
  handler: StatSummaryHandler,
  private val accidentRegisterService: AccidentRegisterService
) {
  private val client = bindToRouterFunction(RouterFunctions.route(REQUEST_PREDICATE, handler)).build()

  @Test
  fun success() {
    // mock
    val scopeType = ScopeType.Monthly
    val from = 201801
    val to = 201812
    val resultList = ArrayList<AccidentRegisterDto4StatSummary>()
    IntStream.range(0, 12).forEach({
      resultList.add(AccidentRegisterDto4StatSummary(
        scope = "2018 年 ${it + 1} 月",
        total = random(0, 100),
        checked = random(0, 100),
        checking = random(0, 100),
        drafting = random(0, 100),
        overdueDraft = random(0, 100),
        overdueRegister = random(0, 100)
      ))
    })
    resultList.reverse()
    `when`(accidentRegisterService.statSummary(scopeType, from, to))
      .thenReturn(resultList.toFlux())

    // invoke
    client.get().uri("/accident-register/stat/summary/$scopeType?from=$from&to=$to")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.length()").isEqualTo(resultList.size)
      .jsonPath("$.[0].scope").isEqualTo("2018 年 12 月")
      .jsonPath("$.[0].total").isEqualTo(resultList[0].total)
      .jsonPath("$.[0].checked").isEqualTo(resultList[0].checked)
      .jsonPath("$.[0].checking").isEqualTo(resultList[0].checking)
      .jsonPath("$.[0].drafting").isEqualTo(resultList[0].drafting)
      .jsonPath("$.[0].overdueDraft").isEqualTo(resultList[0].overdueDraft)
      .jsonPath("$.[0].overdueRegister").isEqualTo(resultList[0].overdueRegister)
      .jsonPath("$.[1].scope").isEqualTo("2018 年 11 月")
      .jsonPath("$.[11].scope").isEqualTo("2018 年 1 月")


    // verify
    verify(accidentRegisterService).statSummary(scopeType, from, to)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val scopeType = ScopeType.Monthly
    val from = 201801
    val to = 201812
    `when`(accidentRegisterService.statSummary(scopeType, from, to)).thenReturn(Flux.error(PermissionDeniedException()))

    // invoke
    val response = client.get().uri("/accident-register/stat/summary/$scopeType?from=$from&to=$to").exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(Utils.TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).statSummary(scopeType, from, to)
  }
}