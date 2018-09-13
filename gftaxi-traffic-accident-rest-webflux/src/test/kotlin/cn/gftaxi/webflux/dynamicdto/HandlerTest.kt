package cn.gftaxi.webflux.dynamicdto

import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import javax.json.Json

/**
 * Test [org.springframework.format.annotation.DateTimeFormat]。
 *
 * See [1.11.3. Conversion, formatting](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-config-conversion)
 *
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, PatchHandler::class, GetHandler::class)
@WebFluxTest
class HandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val patchHandler: PatchHandler,
  private val getHandler: GetHandler
) {
  @Configuration
  class Cfg {
    @Bean
    fun getRoute(handler: GetHandler): RouterFunction<ServerResponse> = route(GetHandler.REQUEST_PREDICATE, handler)

    @Bean
    fun patchRoute(handler: PatchHandler): RouterFunction<ServerResponse> = route(PatchHandler.REQUEST_PREDICATE, handler)
  }

  @Test
  fun patch() {
    // data
    val data = Json.createObjectBuilder()
      .add("name", "test")
      .add("offsetDateTime", "2018-10-01 08:30")
      .build()

    // invoke
    val response = client.patch().uri("/")
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(data.toString())
      .exchange()

    // verify
    response
      .expectStatus().isOk
      .expectBody()
      //.consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.offsetDateTime").isEqualTo(data.getString("offsetDateTime"))
      .jsonPath("$.name").isEqualTo(data.getString("name"))
      .jsonPath("$.code").doesNotExist()
  }

  @Test
  fun get() {
    // invoke
    val response = client.get().uri("/")
      .exchange()

    // verify
    response.expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.name").isEqualTo("test")
      .jsonPath("$.offsetDateTime").isEqualTo("2018-10-01 08:30")
      .jsonPath("$.code").doesNotExist()
  }
}