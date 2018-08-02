package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.Utils
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.GetByCodeHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.route
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import java.time.OffsetDateTime

/**
 * Test [GetByCodeHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(GetByCodeHandler::class)
@EnableWebFlux
@MockBean(AccidentRegisterService::class)
class GetByCodeHandlerTest @Autowired constructor(
  handler: GetByCodeHandler,
  private val accidentRegisterService: AccidentRegisterService
) {
  private val client = bindToRouterFunction(route(REQUEST_PREDICATE, handler)).build()
  private fun randomDto(code: String): AccidentRegisterDto4Form {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Form(
      code = code,
      status = Draft,
      carPlate = "粤A.00001",
      driverName = "driver1",
      driverType = Official,
      draftTime = OffsetDateTime.of(2018, 1, 1, 10, 0, 0, 0, now.offset),
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      locationOther = "芳村上市路"
    )
  }

  @Test
  fun codeExists() {
    val code = "20180101_01"
    // mock
    val dto = randomDto(code = code)
    `when`(accidentRegisterService.getByCode(code)).thenReturn(Mono.just(dto))

    // invoke
    val response = client.get().uri("/accident-register/code/$code").exchange()

    // verify
    response
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.code").isEqualTo(code)
      .jsonPath("$.status").isEqualTo(dto.status.name)
      .jsonPath("$.carPlate").isEqualTo(dto.carPlate)
      .jsonPath("$.driverName").isEqualTo(dto.driverName)
      .jsonPath("$.driverType").isEqualTo(dto.driverType!!.name)
      .jsonPath("$.draftTime").isEqualTo(dto.draftTime.format(Utils.FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.happenTime").isEqualTo(dto.happenTime.format(Utils.FORMAT_DATE_TIME_TO_MINUTE))
      .jsonPath("$.locationOther").isEqualTo(dto.locationOther)
    verify(accidentRegisterService).getByCode(code)
  }

  @Test
  fun codeNotExists() {
    // mock
    val code = "20180101_01"
    `when`(accidentRegisterService.getByCode(code)).thenReturn(Mono.error(NotFoundException()))

    // invoke
    val response = client.get().uri("/accident-register/code/$code").exchange()

    // verify
    response.expectStatus().isNotFound.expectBody().isEmpty
    verify(accidentRegisterService).getByCode(code)
  }
}