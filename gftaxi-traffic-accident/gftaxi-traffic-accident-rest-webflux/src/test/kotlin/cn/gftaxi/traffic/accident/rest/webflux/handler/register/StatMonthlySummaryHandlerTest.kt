package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMM
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatMonthlySummaryHandler.Companion.REQUEST_PREDICATE
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
import java.time.YearMonth

/**
 * Test [StatMonthlySummaryHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, StatMonthlySummaryHandler::class)
@MockBean(AccidentStatService::class)
@WebFluxTest
class StatMonthlySummaryHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentStatService: AccidentStatService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: StatMonthlySummaryHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val url = "/accident-register/stat-monthly-summary"

  @Test
  fun `Success stat`() {
    // mock
    val from = YearMonth.of(2018, 1)
    val to = YearMonth.of(2018, 12)
    val list = (1..12).map { randomAccidentRegisterDto4StatSummary(scope = "${201800 + it}") }
    `when`(accidentStatService.statRegisterMonthlySummary(from, to)).thenReturn(list.toFlux())

    // invoke and verify
    val t = client.get()
      .uri("$url?from=${from.format(FORMAT_TO_YYYYMM)}&to=${to.format(FORMAT_TO_YYYYMM)}")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.length()").isEqualTo(list.size)

    (0..11).forEach {
      t.jsonPath("$.[$it].scope").isEqualTo(list[it].scope)
      t.jsonPath("$.[$it].total").isEqualTo(list[it].total)
      t.jsonPath("$.[$it].checked").isEqualTo(list[it].checked)
      t.jsonPath("$.[$it].checking").isEqualTo(list[it].checking)
      t.jsonPath("$.[$it].drafting").isEqualTo(list[it].drafting)
      t.jsonPath("$.[$it].overdueDraft").isEqualTo(list[it].overdueDraft)
      t.jsonPath("$.[$it].overdueRegister").isEqualTo(list[it].overdueRegister)
    }

    verify(accidentStatService).statRegisterMonthlySummary(from, to)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val from = YearMonth.of(2018, 1)
    val to = YearMonth.of(2018, 12)
    `when`(accidentStatService.statRegisterMonthlySummary(from, to))
      .thenReturn(Flux.error(PermissionDeniedException()))

    // invoke
    val response = client.get()
      .uri("$url?from=${from.format(FORMAT_TO_YYYYMM)}&to=${to.format(FORMAT_TO_YYYYMM)}")
      .exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentStatService).statRegisterMonthlySummary(from, to)
  }
}