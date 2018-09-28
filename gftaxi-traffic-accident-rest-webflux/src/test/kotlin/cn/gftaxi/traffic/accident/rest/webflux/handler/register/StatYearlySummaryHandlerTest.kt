package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatYearlySummaryHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentStatService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentRegisterDto4StatSummary
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8
import java.time.Year

/**
 * Test [StatYearlySummaryHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, StatYearlySummaryHandler::class)
@MockBean(AccidentStatService::class)
@WebFluxTest
class StatYearlySummaryHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentStatService: AccidentStatService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: StatYearlySummaryHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val url = "/accident-register/stat-yearly-summary"

  @Test
  fun `Success stat`() {
    // mock
    val from = Year.of(2017)
    val to = Year.of(2018)
    val list = (0..1).map { randomAccidentRegisterDto4StatSummary(scope = "${2017 + it}") }
    `when`(accidentStatService.statRegisterYearlySummary(from, to)).thenReturn(list.toFlux())

    // invoke and verify
    val t = client.get().uri("$url?from=$from&to=$to")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.length()").isEqualTo(list.size)

    (0..1).forEach {
      t.jsonPath("$.[$it].scope").isEqualTo(list[it].scope)
      t.jsonPath("$.[$it].total").isEqualTo(list[it].total)
      t.jsonPath("$.[$it].checked").isEqualTo(list[it].checked)
      t.jsonPath("$.[$it].checking").isEqualTo(list[it].checking)
      t.jsonPath("$.[$it].drafting").isEqualTo(list[it].drafting)
      t.jsonPath("$.[$it].overdueDraft").isEqualTo(list[it].overdueDraft)
      t.jsonPath("$.[$it].overdueRegister").isEqualTo(list[it].overdueRegister)
    }

    verify(accidentStatService).statRegisterYearlySummary(from, to)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val from = Year.of(2017)
    val to = Year.of(2018)
    `when`(accidentStatService.statRegisterYearlySummary(from, to))
      .thenReturn(Flux.error(PermissionDeniedException()))

    // invoke and verify
    client.get().uri("$url?from=$from&to=$to")
      .exchange()
      .expectStatus().isForbidden.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentStatService).statRegisterYearlySummary(from, to)
  }
}