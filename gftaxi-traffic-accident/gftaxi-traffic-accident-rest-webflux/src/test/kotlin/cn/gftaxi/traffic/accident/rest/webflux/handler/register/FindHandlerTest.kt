package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4View
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.FindHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentRegisterDto4View
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
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, FindHandler::class)
@MockBean(AccidentRegisterService::class)
@WebFluxTest
class FindHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentRegisterService: AccidentRegisterService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: FindHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private val url = "/accident-register"

  @Test
  fun `Found nothing`() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val emptyList = listOf<AccidentRegisterDto4View>()
    `when`(accidentRegisterService.find())
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
    verify(accidentRegisterService).find()
  }

  @Test
  fun `Found something`() {
    // 没指定状态
    findByStatus()

    // 指定一个状态
    AuditStatus.values().forEach { findByStatus(listOf(it)) }

    // 指定多个状态
    findByStatus(AuditStatus.values().toList())
  }

  private fun findByStatus(statuses: List<AuditStatus>? = null) {
    // mock
    reset(accidentRegisterService)
    val pageNo = 1
    val pageSize = 25
    val dto = randomAccidentRegisterDto4View()
    val list = listOf(dto)
    `when`(accidentRegisterService.find(pageNo, pageSize, statuses))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo - 1, pageSize), list.size.toLong())))

    // invoke
    client.get().uri("$url?pageNo=$pageNo&pageSize=$pageSize" +
      (statuses?.run { "&status=${statuses.joinToString(",")}" } ?: ""))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      //.consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.count").isEqualTo(list.size)
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].code").isEqualTo(dto.code!!)

    // verify
    verify(accidentRegisterService).find(pageNo, pageSize, statuses)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(accidentRegisterService.find()).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke and verify
    client.get().uri(url).exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).find()
  }
}