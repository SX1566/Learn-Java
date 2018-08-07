package cn.gftaxi.webflux.dynamicdto

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.route
import javax.json.Json

/**
 * Test [org.springframework.format.annotation.DateTimeFormat]。
 *
 * See [1.11.3. Conversion, formatting](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-config-conversion)
 *
 * @author RJ
 */
@SpringJUnitConfig(PatchHandler::class, GetHandler::class)
@EnableWebFlux
class HandlerTest @Autowired constructor(
  private val patchHandler: PatchHandler,
  private val getHandler: GetHandler
) {
  @Test
  fun patch() {
    val client = bindToRouterFunction(route(PatchHandler.REQUEST_PREDICATE, patchHandler)).build()
    // mock
    val id = 1
    val data = Json.createObjectBuilder()
      .add("name", "test")
      // Use Dto/@set:DateTimeFormat - 实测并没有用上，但 webflux 却可以灵活的转换多种格式
      .add("offsetDateTime", "2018-10-01 08:30")

    // invoke
    val response = client.patch().uri("/date-time/$id")
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .syncBody(data.build().toString())
      .exchange()

    // verify
    response.expectStatus().isNoContent.expectBody().isEmpty
  }

  @Test
  fun get() {
    val client = bindToRouterFunction(route(GetHandler.REQUEST_PREDICATE, getHandler)).build()
    // mock
    val id = 1

    // invoke
    val response = client.get().uri("/date-time/$id")
      .exchange()

    // verify
    response.expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.name").isEqualTo("test")
      .jsonPath("$.offsetDateTime").isEqualTo("2018-10-01 08:30:20") // Use Dto/@get:JsonFormat
      .jsonPath("$.code").doesNotExist()
  }
}