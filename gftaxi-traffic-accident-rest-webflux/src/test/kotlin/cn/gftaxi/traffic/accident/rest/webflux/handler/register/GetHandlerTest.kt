package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.Utils
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.GetHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import java.time.OffsetDateTime


/**
 * Test [GetHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, GetHandler::class)
@MockBean(AccidentRegisterService::class)
@WebFluxTest
class GetHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentRegisterService: AccidentRegisterService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: GetHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  private fun randomDto(id: Int): AccidentRegisterDto4Form {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Form().apply {
      this.id = id
      code = "20180101_01"
      status = Draft
      carPlate = "粤A.00001"
      driverName = "driver1"
      driverType = Official
      createTime = OffsetDateTime.of(2018, 1, 1, 10, 0, 0, 0, now.offset)
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset)
      location = "芳村上市路"
    }
  }

  @Test
  fun success() {
    val id = 1
    // mock
    val dto = randomDto(id = id)
    `when`(accidentRegisterService.get(id)).thenReturn(Mono.just(dto))

    // invoke
    val response = client.get().uri("/accident-register/$id").exchange()

    // verify
    response
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      //.consumeWith { println(String(it.responseBody!!)) }
      .jsonPath("$.id").isEqualTo(id)
      .jsonPath("$.code").isEqualTo(dto.code!!)
      .jsonPath("$.status").isEqualTo(dto.status!!.name)
      .jsonPath("$.carPlate").isEqualTo(dto.carPlate!!)
      .jsonPath("$.driverName").isEqualTo(dto.driverName!!)
      .jsonPath("$.driverType").isEqualTo(dto.driverType!!.name)
      .jsonPath("$.createTime").isEqualTo(dto.createTime!!.format(Utils.FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.happenTime").isEqualTo(dto.happenTime!!.format(Utils.FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.location").isEqualTo(dto.location!!)
    verify(accidentRegisterService).get(id)
  }

  @Test
  fun failedByNotFound() {
    // mock
    val id = 1
    `when`(accidentRegisterService.get(id)).thenReturn(Mono.error(NotFoundException()))

    // invoke
    val response = client.get().uri("/accident-register/$id").exchange()

    // verify
    response.expectStatus().isNotFound.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).get(id)
  }

  @Test
  fun failedByPermissionDenied() {
    // mock
    val id = 1
    `when`(accidentRegisterService.get(id)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val response = client.get().uri("/accident-register/$id").exchange()

    // verify
    response.expectStatus().isForbidden.expectHeader().contentType(TEXT_PLAIN_UTF8)
    verify(accidentRegisterService).get(id)
  }
}