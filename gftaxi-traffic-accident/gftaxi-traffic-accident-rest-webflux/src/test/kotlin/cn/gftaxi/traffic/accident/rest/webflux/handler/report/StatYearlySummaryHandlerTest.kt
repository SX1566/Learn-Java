package cn.gftaxi.traffic.accident.rest.webflux.handler.report

import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.StatYearlySummaryHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentStatService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentReportDto4StatSummary
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
 * @author zh
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

  private val url = "/accident-report/stat-yearly-summary"

  @Test
  fun `Success stat`() {
    // mock
    val from = Year.of(2017)
    val to = Year.of(2018)
    val list = (0..1).map { randomAccidentReportDto4StatSummary(scope = "${2017 + it}") }
    `when`(accidentStatService.statReportYearlySummary(from, to)).thenReturn(list.toFlux())

    // invoke and verify
    val t = client.get().uri("$url?from=$from&to=$to")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.length()").isEqualTo(list.size)

    list.forEachIndexed { index, dto ->
      t.jsonPath("$.[$index].scope").isEqualTo(dto.scope)
      t.jsonPath("$.[$index].total").isEqualTo(dto.total)
      t.jsonPath("$.[$index].checked").isEqualTo(dto.checked)
      t.jsonPath("$.[$index].checking").isEqualTo(dto.checking)
      t.jsonPath("$.[$index].closed").isEqualTo(dto.closed)
      t.jsonPath("$.[$index].reporting").isEqualTo(dto.reporting)
      t.jsonPath("$.[$index].overdueReport").isEqualTo(dto.overdueReport)
    }

    verify(accidentStatService).statReportYearlySummary(from, to)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val from = Year.of(2017)
    val to = Year.of(2018)
    `when`(accidentStatService.statReportYearlySummary(from, to)).thenReturn(Flux.error(PermissionDeniedException()))

    // invoke and verify
    client.get().uri("$url?from=$from&to=$to")
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentStatService).statReportYearlySummary(from, to)
  }
}