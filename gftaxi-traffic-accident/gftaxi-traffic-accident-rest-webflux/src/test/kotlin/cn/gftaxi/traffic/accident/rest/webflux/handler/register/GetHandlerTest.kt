package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.common.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.GetHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.test.TestUtils
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentRegisterDto4Form
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
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * Test [GetHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, GetHandler::class)
@MockBean(AccidentRegisterService::class)
@WebFluxTest
class GetHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentRegisterService: AccidentRegisterService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: GetHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val id = TestUtils.randomInt()
  private val url = "/accident-register/$id"

  @Test
  fun `Success get`() {
    // mock
    val dto = randomAccidentRegisterDto4Form(id = id)
    `when`(accidentRegisterService.get(id)).thenReturn(Mono.just(dto))

    // invoke and verify
    client.get().uri(url).exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      //.consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.id").isEqualTo(id)
      .jsonPath("$.code").isEqualTo(dto.code!!)
      .jsonPath("$.draftStatus").isEqualTo(dto.draftStatus!!.name)
      .jsonPath("$.draftTime").isEqualTo(dto.draftTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.overdueDraft").isEqualTo(dto.overdueDraft!!)
      .jsonPath("$.registerStatus").isEqualTo(dto.registerStatus!!.name)
      .jsonPath("$.registerTime").isEqualTo(dto.registerTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.overdueRegister").isEqualTo(dto.overdueRegister!!)
      .jsonPath("$.carPlate").isEqualTo(dto.carPlate!!)
      .jsonPath("$.driverName").isEqualTo(dto.driverName!!)
      .jsonPath("$.driverType").isEqualTo(dto.driverType!!.name)
      .jsonPath("$.happenTime").isEqualTo(dto.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.location").isEqualTo(dto.location!!)
    verify(accidentRegisterService).get(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    `when`(accidentRegisterService.get(id)).thenReturn(Mono.empty())

    // invoke and verify
    client.get().uri(url).exchange()
      .expectStatus().isNotFound.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).get(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(accidentRegisterService.get(id)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    client.get().uri(url).exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).get(id)
  }
}