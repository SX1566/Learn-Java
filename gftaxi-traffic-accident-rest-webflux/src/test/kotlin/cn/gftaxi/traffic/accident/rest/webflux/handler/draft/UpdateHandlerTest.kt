package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.UpdateHandler.Companion.REQUEST_PREDICATE
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
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import javax.json.Json

/**
 * Test [UpdateHandler]。
 *
 * @author cjw
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, UpdateHandler::class)
@MockBean(AccidentDraftService::class)
@WebFluxTest
class UpdateHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentDraftService: AccidentDraftService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: UpdateHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  @Test
  fun updateBySuccess() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder()
      .add("carPlate", "粤A.N3402")
      .add("driverName", "driver")
    `when`(accidentDraftService.update(any(), any())).thenReturn(Mono.empty())

    // invoke
    val response = client.patch().uri("/accident-draft/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNoContent.expectBody().isEmpty
    verify(accidentDraftService).update(any(), any())
  }

  @Test
  fun updateByNotFound() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder()
      .add("carPlate", "粤A.N3402")
      .add("driverName", "driver")
    `when`(accidentDraftService.update(any(), any())).thenReturn(Mono.error(NotFoundException("指定的案件不存在")))

    // invoke
    val response = client.patch().uri("/accident-draft/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNotFound.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentDraftService).update(any(), any())
  }
}