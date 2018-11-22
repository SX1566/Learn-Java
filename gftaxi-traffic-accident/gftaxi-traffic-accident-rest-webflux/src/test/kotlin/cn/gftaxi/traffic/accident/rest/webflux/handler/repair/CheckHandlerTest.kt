package cn.gftaxi.traffic.accident.rest.webflux.handler.repair

import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.repair.FindHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.RepairService
import cn.gftaxi.traffic.accident.test.TestUtils.randomInt
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
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
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * Test [CheckHandler]
 */
@SpringJUnitConfig(UnitTestConfiguration::class, CheckHandler::class)
@MockBean(RepairService::class)
@WebFluxTest
class CheckHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val RepairService: RepairService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: CheckHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val id = randomInt()
  private val url = "/accident-repair/checked/$id"
  private val testBodyData = """{"passed:" true}"""
  private val checkedInfo = CheckedInfoDto(passed = true)

  @Test
  fun `Success checked`() {
    //mock
    `when`(RepairService.checked(id, checkedInfo)).thenReturn(Mono.empty())

    //invoke and verify
    client.post().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isNoContent
      .expectBody().isEmpty
    verify(RepairService).checked(id, checkedInfo)
  }

  @Test
  fun failedByNotFound() {
    //mock
    `when`(RepairService.checked(id, checkedInfo)).thenReturn(Mono.error(NotFoundException()))

    //invoke and verify
    client.post().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(RepairService).checked(id, checkedInfo)
  }

  @Test
  fun failedByForbidden() {
    //mock
    `when`(RepairService.checked(id, checkedInfo)).thenReturn(Mono.error(ForbiddenException()))

    //invoke and verify
    client.post().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(RepairService).checked(id, checkedInfo)
  }

  @Test
  fun failedByPermissionDenied(){
    //mock
    `when`(RepairService.checked(id,checkedInfo)).thenReturn(Mono.error(PermissionDeniedException()))

    //invoke and verify
    client.post().uri(url)
      .contentType(APPLICATION_JSON_UTF8)
      .syncBody(testBodyData)
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(RepairService).checked(id, checkedInfo)
  }
}


