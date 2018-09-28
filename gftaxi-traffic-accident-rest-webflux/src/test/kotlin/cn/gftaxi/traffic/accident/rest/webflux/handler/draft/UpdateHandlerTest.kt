package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.UpdateHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
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
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

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

  private val id = randomInt()
  private val url = "/accident-draft/$id"
  private val testBodyData = """{"carPlate": "test"}"""

  @Test
  fun `Success update`() {
    // mock
    `when`(accidentDraftService.update(any(), any())).thenReturn(Mono.empty())

    // invoke and verify
    client.patch().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isNoContent
      .expectBody().isEmpty
    verify(accidentDraftService).update(any(), any())
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    `when`(accidentDraftService.update(any(), any())).thenReturn(Mono.error(NotFoundException()))

    // invoke and verify
    client.patch().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentDraftService).update(any(), any())
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(accidentDraftService.update(any(), any())).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    client.patch().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentDraftService).update(any(), any())
  }
}