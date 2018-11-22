package cn.gftaxi.traffic.accident.rest.webflux.handler.repair

import cn.gftaxi.traffic.accident.common.MaintainStatus
import cn.gftaxi.traffic.accident.dto.RepairDto4View
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.repair.FindHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.RepairService
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentRepairDto4View
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
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
 * Test[FindHandler]
 * @author sx
 */

@SpringJUnitConfig(UnitTestConfiguration::class, FindHandler::class)
@MockBean(RepairService::class)
@WebFluxTest
class FindHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val repairService: RepairService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: FindHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)

  }

  private val url = "/accident-repair"

  @Test
  fun `Found nothing`() {
    //mock
    val pageNo = 1
    val pageSize = 25
    val emptyList = listOf<RepairDto4View>()
    `when`(repairService.find())
      .thenReturn(Mono.just(PageImpl(emptyList, PageRequest.of(pageNo - 1, pageSize), 0)))

    //invoke
    client.get().uri(url)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.count").isEqualTo(0)
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows").isEmpty

    //verify
    verify(repairService).find()
  }

  @Test
  fun `Found something`() {
    //没指定状态
    findByStatus()
    //指定一个状态
    MaintainStatus.values().forEach { findByStatus(listOf(it)) }
    //指定多个状态
    findByStatus(MaintainStatus.values().toList())

  }

  private fun findByStatus(statuses: List<MaintainStatus>? = null) {
    //mock
    reset(repairService)
    val pageNo = 1
    val pageSize = 25
    val dto = randomAccidentRepairDto4View()
    val list = listOf(dto)
    `when`(repairService.find(pageNo, pageSize, statuses))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo - 1, pageSize), list.size.toLong())))

    //invoke
    client.get().uri("$url?pageNo=$pageNo&pageSize=$pageSize" +
      (statuses?.run { "&status=${statuses.joinToString { "," }}" } ?: ""))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
//      .consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.count").isEqualTo(list.size)
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].code").isEqualTo(dto.code!!)

    //verify
    verify(repairService).find(pageNo, pageSize, statuses)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    //mock
    `when`(repairService.find()).thenReturn(Mono.error(PermissionDeniedException()))

    //invoke and verify
    client.get().uri(url).exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(repairService).find()

  }

}