package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.UpdateHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import com.nhaarman.mockito_kotlin.any
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
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import javax.json.Json

/**
 * Test [UpdateHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(UpdateHandler::class)
@EnableWebFlux
@MockBean(AccidentRegisterService::class)
class UpdateHandlerTest @Autowired constructor(
  handler: UpdateHandler,
  private val accidentRegisterService: AccidentRegisterService
) {
  private val client = bindToRouterFunction(route(REQUEST_PREDICATE, handler)).build()

  @Test
  fun success() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("carPlate", "test")
    `when`(accidentRegisterService.update(any(), any())).thenReturn(Mono.empty())

    // invoke
    val response = client.patch().uri("/accident-register/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNoContent.expectBody().isEmpty
    verify(accidentRegisterService).update(any(), any())
  }

  @Test
  fun failedByNotFound() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("carPlate", "test")
    `when`(accidentRegisterService.update(any(), any())).thenReturn(Mono.error(NotFoundException()))

    // invoke
    val response = client.patch().uri("/accident-register/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNotFound.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).update(any(), any())
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val id = 1
    val data = Json.createObjectBuilder().add("carPlate", "test")
    `when`(accidentRegisterService.update(any(), any())).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val response = client.patch().uri("/accident-register/$id")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).update(any(), any())
  }
}