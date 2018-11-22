package cn.gftaxi.traffic.accident.rest.webflux.handler.report

import cn.gftaxi.traffic.accident.common.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.GetHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentReportDto4Form
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * Test [GetHandler]
 *
 * @author zh
 */
@SpringJUnitConfig(UnitTestConfiguration::class, GetHandler::class)
@MockBean(AccidentReportService::class)
@WebFluxTest
internal class GetHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val service: AccidentReportService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: GetHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val id = randomInt()
  private val url = "/accident-report/$id"

  @Test
  fun success() {
    // mock
    val dto = randomAccidentReportDto4Form(id = id)
    `when`(service.get(id)).thenReturn(Mono.just(dto))

    // invoke
    client.get().uri(url)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.id").isEqualTo(dto.id!!)
      .jsonPath("$.code").isEqualTo(dto.code!!)
      .jsonPath("$.happenTime").isEqualTo(dto.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.draftTime").isEqualTo(dto.draftTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.overdueDraft").isEqualTo(dto.overdueDraft!!)
      .jsonPath("$.draftStatus").isEqualTo(dto.draftStatus!!.name)
      .jsonPath("$.registerTime").isEqualTo(dto.registerTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.overdueRegister").isEqualTo(dto.overdueRegister!!)
      .jsonPath("$.registerStatus").isEqualTo(dto.registerStatus!!.name)
      .jsonPath("$.reportTime").isEqualTo(dto.reportTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.overdueReport").isEqualTo(dto.overdueReport!!)
      .jsonPath("$.reportStatus").isEqualTo(dto.reportStatus!!.name)
      .jsonPath("$.motorcadeName").isEqualTo(dto.motorcadeName!!)
      .jsonPath("$.carPlate").isEqualTo(dto.carPlate!!)
      .jsonPath("$.driverName").isEqualTo(dto.driverName!!)
      .jsonPath("$.driverType").isEqualTo(dto.driverType!!.name)
      .jsonPath("$.location").isEqualTo(dto.location!!)
      .jsonPath("$.hitForm").isEqualTo(dto.hitForm!!)
      .jsonPath("$.hitType").isEqualTo(dto.hitType!!)
      .jsonPath("$.authorName").isEqualTo(dto.authorName!!)

    // verify
    verify(service).get(id)
  }

  @Test
  fun failedByNotFound() {
    // mock
    `when`(service.get(id)).thenReturn(Mono.error(NotFoundException()))

    // invoke
    client.get().uri(url)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(TEXT_PLAIN_UTF8)

    // verify
    verify(service).get(id)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(service.get(id)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    client.get().uri(url)
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)

    // verify
    verify(service).get(id)
  }
}