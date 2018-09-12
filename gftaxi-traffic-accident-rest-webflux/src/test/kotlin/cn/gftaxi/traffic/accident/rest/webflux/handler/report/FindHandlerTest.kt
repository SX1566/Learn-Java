package cn.gftaxi.traffic.accident.rest.webflux.handler.report


import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentReport.Status
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.FindHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentReportService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
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
import java.time.OffsetDateTime
import java.util.*

/**
 * Test [FindHandler]
 *
 * @author zh
 */
@SpringJUnitConfig(UnitTestConfiguration::class, FindHandler::class)
@MockBean(AccidentReportService::class)
@WebFluxTest
internal class FindHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val service: AccidentReportService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: FindHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private fun randomDto(id: Int, status: Status? = null): AccidentReportDto4View {
    return AccidentReportDto4View(id = id, code = randString(), driverType = DriverType.Official,
      happenTime = OffsetDateTime.now(), overdueCreate = true, status = status)
  }

  private fun randString(): String {
    return UUID.randomUUID().toString()
  }

  @Test
  fun successByNoStatus() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val dao1 = randomDto(1)
    val dao2 = randomDto(2)
    val list = listOf(dao1, dao2)
    val page = PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())
    `when`(service.find(anyInt(), anyInt(), any(), any())).thenReturn(Mono.just(page))

    // invoke
    client.get().uri("/accident-report?pageNo=$pageNo&pageSize=$pageSize")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size.toLong())
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].id").isEqualTo(dao1.id!!)
      .jsonPath("$.rows[0].code").isEqualTo(dao1.code!!)
      .jsonPath("$.rows[0].driverType").isEqualTo(dao1.driverType.toString())
      .jsonPath("$.rows[0].happenTime").isEqualTo(dao1.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.rows[0].overdueCreate").isEqualTo(dao1.overdueCreate!!)
      .jsonPath("$.rows[1].id").isEqualTo(dao2.id!!)
      .jsonPath("$.rows[1].code").isEqualTo(dao2.code!!)
      .jsonPath("$.rows[1].driverType").isEqualTo(dao2.driverType.toString())
      .jsonPath("$.rows[1].happenTime").isEqualTo(dao2.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.rows[1].overdueCreate").isEqualTo(dao2.overdueCreate!!)

    // verify
    verify(service).find(anyInt(), anyInt(), any(), any())
  }

  @Test
  fun successByOneStatus() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val dao1 = randomDto(1, Status.Draft)
    val dao2 = randomDto(2, Status.Draft)
    val list = listOf(dao1, dao2)
    val page = PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())
    `when`(service.find(anyInt(), anyInt(), any(), any())).thenReturn(Mono.just(page))

    // invoke
    client.get().uri("/accident-report?pageNo=$pageNo&pageSize=$pageSize&status=${Status.Draft}")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size.toLong())
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].id").isEqualTo(dao1.id!!)
      .jsonPath("$.rows[0].code").isEqualTo(dao1.code!!)
      .jsonPath("$.rows[0].driverType").isEqualTo(dao1.driverType.toString())
      .jsonPath("$.rows[0].happenTime").isEqualTo(dao1.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.rows[0].overdueCreate").isEqualTo(dao1.overdueCreate!!)
      .jsonPath("$.rows[1].id").isEqualTo(dao2.id!!)
      .jsonPath("$.rows[1].code").isEqualTo(dao2.code!!)
      .jsonPath("$.rows[1].driverType").isEqualTo(dao2.driverType.toString())
      .jsonPath("$.rows[1].happenTime").isEqualTo(dao2.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.rows[1].overdueCreate").isEqualTo(dao2.overdueCreate!!)

    // verify
    verify(service).find(anyInt(), anyInt(), any(), any())
  }

  @Test
  fun successBySomeStatus() {
    // mock
    val pageNo = 1
    val pageSize = 25
    val dao1 = randomDto(1, Status.Draft)
    val dao2 = randomDto(2, Status.Approved)
    val list = listOf(dao1, dao2)
    val page = PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())
    `when`(service.find(anyInt(), anyInt(), any(), any())).thenReturn(Mono.just(page))

    // invoke
    client.get().uri("/accident-report?pageNo=$pageNo&pageSize=$pageSize&status=${Status.Draft},${Status.Approved}")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size.toLong())
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].id").isEqualTo(dao1.id!!)
      .jsonPath("$.rows[0].code").isEqualTo(dao1.code!!)
      .jsonPath("$.rows[0].driverType").isEqualTo(dao1.driverType.toString())
      .jsonPath("$.rows[0].happenTime").isEqualTo(dao1.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.rows[0].overdueCreate").isEqualTo(dao1.overdueCreate!!)
      .jsonPath("$.rows[1].id").isEqualTo(dao2.id!!)
      .jsonPath("$.rows[1].code").isEqualTo(dao2.code!!)
      .jsonPath("$.rows[1].driverType").isEqualTo(dao2.driverType.toString())
      .jsonPath("$.rows[1].happenTime").isEqualTo(dao2.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.rows[1].overdueCreate").isEqualTo(dao2.overdueCreate!!)

    // verify
    verify(service).find(anyInt(), anyInt(), any(), any())
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    `when`(service.find(anyInt(), anyInt(), any(), any())).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    client.get().uri("/accident-report")
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType(TEXT_PLAIN_UTF8)

    // verify
    verify(service).find(anyInt(), anyInt(), any(), any())
  }
}