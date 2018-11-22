package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4View
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.FindHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentDraftDto4View
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * Test [FindHandler]。
 *
 * @author cjw
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, FindHandler::class)
@MockBean(AccidentDraftService::class)
@WebFluxTest
class FindHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentDraftService: AccidentDraftService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler : FindHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val url = "/accident-draft"

  @Test
  fun `Found nothing`() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val emptyList = listOf<AccidentDraftDto4View>()
    `when`(accidentDraftService.find())
      .thenReturn(Mono.just(PageImpl(emptyList, PageRequest.of(pageNo - 1, pageSize), 0)))

    // invoke
    client.get().uri(url)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      //.consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.count").isEqualTo(0)
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows").isEmpty

    // verify
    verify(accidentDraftService).find()
  }

  @Test
  fun `Found something`() {
    // 没指定状态
    findByStatus()

    // 指定一个状态
    DraftStatus.values().forEach { findByStatus(listOf(it)) }

    // 指定多个状态
    findByStatus(DraftStatus.values().toList())
  }

  private fun findByStatus(statuses: List<DraftStatus>? = null) {
    // mock
    reset(accidentDraftService)
    val pageNo = 1
    val pageSize = 25
    val expected = randomAccidentDraftDto4View()
    val list = listOf(expected)
    `when`(accidentDraftService.find(pageNo, pageSize, statuses))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo - 1, pageSize), list.size.toLong())))

    // invoke
    val url = "$url?pageNo=$pageNo&pageSize=$pageSize" +
      (statuses?.run { "&status=${statuses.joinToString(",")}" } ?: "")
    client.get().uri(url)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      //.consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.count").isEqualTo(list.size)
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].code").isEqualTo(expected.code!!)

    // verify
    verify(accidentDraftService).find(pageNo, pageSize, statuses)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(accidentDraftService.find()).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    client.get().uri(url).exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentDraftService).find()
  }
}