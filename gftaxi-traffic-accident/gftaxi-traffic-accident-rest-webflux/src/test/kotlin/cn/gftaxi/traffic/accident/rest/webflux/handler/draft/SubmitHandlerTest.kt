package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.common.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.SubmitHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAuthenticatedUser
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
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
import tech.simter.exception.NonUniqueException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.context.SystemContext
import tech.simter.reactive.security.ReactiveSecurityService
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8
import java.util.*
import javax.json.Json

/**
 * Test [SubmitHandler]。
 *
 * @author cjw
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, SubmitHandler::class)
@MockBean(AccidentDraftService::class, ReactiveSecurityService::class)
@WebFluxTest
class SubmitHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentDraftService: AccidentDraftService,
  private val securityService: ReactiveSecurityService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: SubmitHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val url = "/accident-draft"

  @Test
  fun `Success submit`() {
    // mock
    val pair = randomCase(overdueDraft = false)
    val data = Json.createObjectBuilder()
    `data`.add("carPlate", pair.first.carPlate)
    `data`.add("driverName", pair.first.driverName)
    `data`.add("happenTime", pair.first.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
    `data`.add("location", pair.first.location)
    `data`.add("hitForm", pair.first.hitForm)
    `data`.add("hitType", pair.first.hitType)
    `when`(accidentDraftService.submit(any())).thenReturn(Mono.just(pair))
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(
      SystemContext.User(id = 1, account = pair.second.authorId!!, name = pair.second.authorName!!)
    )))

    // invoke and verify
    client.post().uri(url)
      .header("Content-Type", APPLICATION_JSON_UTF8.toString())
      .syncBody(data.build().toString())
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.id").isEqualTo(pair.first.id!!)
      .jsonPath("$.code").isEqualTo(pair.first.code!!)
      .jsonPath("$.draftTime").isEqualTo(pair.second.draftTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.overdueDraft").isEqualTo(pair.second.overdueDraft!!)
    verify(accidentDraftService).submit(any())
  }

  @Test
  fun `Failed by NonUniqueException`() {
    // mock
    `when`(accidentDraftService.submit(any())).thenReturn(Mono.error(NonUniqueException()))
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(randomAuthenticatedUser())))

    // invoke and verify
    client.post().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody("""{"carPlate": "test"}""")
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(securityService).getAuthenticatedUser()
    verify(accidentDraftService).submit(any())
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(accidentDraftService.submit(any())).thenReturn(Mono.error(PermissionDeniedException()))
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(randomAuthenticatedUser())))

    // invoke and verify
    client.post().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody("""{"carPlate": "test"}""")
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(securityService).getAuthenticatedUser()
    verify(accidentDraftService).submit(any())
  }
}