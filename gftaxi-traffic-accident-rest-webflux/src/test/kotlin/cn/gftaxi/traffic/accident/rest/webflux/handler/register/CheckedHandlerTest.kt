package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.CheckedInfo
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.CheckedHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.route
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import javax.json.Json

/**
 * Test [ToCheckHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(CheckedHandler::class)
@EnableWebFlux
@MockBean(AccidentRegisterService::class)
class CheckedHandlerTest @Autowired constructor(
  handler: CheckedHandler,
  private val accidentRegisterService: AccidentRegisterService
) {
  private val client = bindToRouterFunction(route(REQUEST_PREDICATE, handler)).build()

  @Test
  fun success() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("passed", true)
    val checkedInfo = CheckedInfo(passed = true)
    `when`(accidentRegisterService.checked(id, checkedInfo)).thenReturn(Mono.empty())

    // invoke
    val response = client.post().uri("/accident-register/checked/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNoContent.expectBody().isEmpty
    verify(accidentRegisterService).checked(id, checkedInfo)
  }

  @Test
  fun failedByNotFound() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("passed", true)
    val checkedInfo = CheckedInfo(passed = true)
    `when`(accidentRegisterService.checked(id, checkedInfo)).thenReturn(Mono.error(NotFoundException()))

    // invoke
    val response = client.post().uri("/accident-register/checked/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNotFound.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).checked(id, checkedInfo)
  }

  @Test
  fun failedByForbidden() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("passed", true)
    val checkedInfo = CheckedInfo(passed = true)
    `when`(accidentRegisterService.checked(id, checkedInfo)).thenReturn(Mono.error(ForbiddenException()))

    // invoke
    val response = client.post().uri("/accident-register/checked/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).checked(id, checkedInfo)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("passed", true)
    val checkedInfo = CheckedInfo(passed = true)
    `when`(accidentRegisterService.checked(id, checkedInfo)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val response = client.post().uri("/accident-register/checked/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).checked(id, checkedInfo)
  }
}