package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.ToCheckHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * Test [ToCheckHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, ToCheckHandler::class)
@MockBean(AccidentRegisterService::class)
@WebFluxTest
class ToCheckHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentRegisterService: AccidentRegisterService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: ToCheckHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val id = randomInt()
  private val url = "/accident-register/to-check/$id"

  @Test
  fun `Success submit`() {
    // mock
    `when`(accidentRegisterService.toCheck(id)).thenReturn(Mono.empty())

    // invoke and verify
    client.post().uri(url).exchange()
      .expectStatus().isNoContent.expectBody().isEmpty
    verify(accidentRegisterService).toCheck(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    `when`(accidentRegisterService.toCheck(id)).thenReturn(Mono.error(NotFoundException()))

    // invoke and verify
    client.post().uri(url).exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).toCheck(id)
  }

  @Test
  fun `Failed by Forbidden`() {
    // mock
    `when`(accidentRegisterService.toCheck(id)).thenReturn(Mono.error(ForbiddenException()))

    // invoke and verify
    client.post().uri(url).exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).toCheck(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(accidentRegisterService.toCheck(id)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    client.post().uri(url).exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).toCheck(id)
  }
}