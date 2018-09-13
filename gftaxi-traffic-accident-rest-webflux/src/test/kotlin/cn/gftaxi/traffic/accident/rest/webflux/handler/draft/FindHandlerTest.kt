package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import cn.gftaxi.traffic.accident.rest.webflux.TestUtils.randomAccidentDraft
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.FindHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
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
import java.util.*

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
    fun theRoute(handler: FindHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  @Test
  fun find() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val search = "2018"
    val status = Status.Todo
    val code = "20180709_01"
    val list = ArrayList<AccidentDraft>()
    list.add(randomAccidentDraft(code = code))
    `when`(accidentDraftService.find(pageNo, pageSize, status, search))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())))

    // invoke
    client.get().uri("/accident-draft?pageNo=$pageNo&pageSize=$pageSize&status=$status&search=$search")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size) // verify count
      .jsonPath("$.pageNo").isEqualTo(pageNo)     // verify pageNo
      .jsonPath("$.pageSize").isEqualTo(pageSize) // verify pageSize
      .jsonPath("$.rows[0].code").isEqualTo(code)    // verify AccidentDraft.code

    // verify
    verify(accidentDraftService).find(pageNo, pageSize, status, search)
  }
}