package cn.gftaxi.traffic.accident.rest.webflux.handler.report

import cn.gftaxi.traffic.accident.rest.webflux.handler.report.ToCheckHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.route
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * Test [ToCheckHandler]。
 *
 * @author zh
 */
@SpringJUnitConfig(ToCheckHandler::class)
@EnableWebFlux
@MockBean(AccidentReportService::class)
internal class ToCheckHandlerTest @Autowired constructor(
  handler: ToCheckHandler,
  private val service: AccidentReportService
) {
  private val client = bindToRouterFunction(route(REQUEST_PREDICATE, handler)).build()
  private val id = randomInt()
  private val url = "/accident-report/to-check/$id"

  @Test
  fun success() {
    // mock
    `when`(service.toCheck(id)).thenReturn(Mono.empty())

    // invoke
    val response = client.post().uri(url).exchange()

    // verify
    response.expectStatus().isNoContent.expectBody().isEmpty
    verify(service).toCheck(id)
  }

  @Test
  fun failedByNotFound() {
    // mock
    `when`(service.toCheck(id)).thenReturn(Mono.error(NotFoundException()))

    // invoke
    val response = client.post().uri(url).exchange()

    // verify
    response.expectStatus().isNotFound
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(service).toCheck(id)
  }

  @Test
  fun failedByForbidden() {
    // mock
    `when`(service.toCheck(id)).thenReturn(Mono.error(ForbiddenException()))

    // invoke
    val response = client.post().uri(url).exchange()

    // verify
    response.expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(service).toCheck(id)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(service.toCheck(id)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val response = client.post().uri(url).exchange()

    // verify
    response.expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(service).toCheck(id)
  }

}