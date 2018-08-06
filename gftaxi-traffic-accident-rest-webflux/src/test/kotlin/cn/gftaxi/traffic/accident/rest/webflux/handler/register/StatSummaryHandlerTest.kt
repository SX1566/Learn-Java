package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
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
import tech.simter.exception.PermissionDeniedException
import java.util.*

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
    val dto = AccidentRegisterDto4StatSummary(
      scope = "本月",
      total = random(0, 100),
      checked = random(0, 100),
      checking = random(0, 100),
      drafting = random(0, 100),
      overdueDraft = random(0, 100),
      overdueRegister = random(0, 100)
    )
    `when`(accidentRegisterService.statSummary())
      .thenReturn(Flux.just(dto, dto.copy(scope = "上月"), dto.copy(scope = "本年")))

    // invoke
    client.get().uri("/accident-register/stat/summary")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.[0].scope").isEqualTo("本月")
      .jsonPath("$.[0].total").isEqualTo(dto.total)
      .jsonPath("$.[0].checked").isEqualTo(dto.checked)
      .jsonPath("$.[0].checking").isEqualTo(dto.checking)
      .jsonPath("$.[0].drafting").isEqualTo(dto.drafting)
      .jsonPath("$.[0].overdueDraft").isEqualTo(dto.overdueDraft)
      .jsonPath("$.[0].overdueRegister").isEqualTo(dto.overdueRegister)
      .jsonPath("$.[1].scope").isEqualTo("上月")
      .jsonPath("$.[2].scope").isEqualTo("本年")

    // verify
    verify(accidentRegisterService).statSummary()
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(accidentRegisterService.statSummary()).thenReturn(Flux.error(PermissionDeniedException()))

    // invoke
    val response = client.get().uri("/accident-register/stat/summary").exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(Utils.TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).statSummary()
  }
}