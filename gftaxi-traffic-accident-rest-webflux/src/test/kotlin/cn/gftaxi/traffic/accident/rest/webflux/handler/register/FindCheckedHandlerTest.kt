package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.FindCheckedHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.route
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Test [FindCheckedHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(FindCheckedHandler::class)
@EnableWebFlux
@MockBean(AccidentRegisterService::class)
class FindCheckedHandlerTest @Autowired constructor(
  handler: FindCheckedHandler,
  private val accidentRegisterService: AccidentRegisterService
) {
  private val client = bindToRouterFunction(route(REQUEST_PREDICATE, handler)).build()

  private fun randomDto(code: String): AccidentRegisterDto4Checked {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Checked(
      code = code,
      carPlate = "粤A.00001",
      driverName = "driver1",
      driverType = Official,
      location = "虚拟地址",
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      checkedComment = null,
      checkerName = "gftaxi",
      checkedCount = 1,
      checkedTime = OffsetDateTime.now()
    )
  }

  @Test
  fun findBoth() {
    findByStatus(null)
  }

  @Test
  fun findApprovedOnly() {
    findByStatus(Approved)
  }

  @Test
  fun findRejectedOnly() {
    findByStatus(Rejected)
  }

  private fun findByStatus(status: Status?) {
    // mock
    val pageNo = 1
    val pageSize = 25
    var code = 1
    val dto = randomDto(code = "20180101_0$code")
    val list = listOf(dto.copy(code = "20180101_0${++code}"), dto)
    `when`(accidentRegisterService.findChecked(pageNo, pageSize, status, null))
      .thenReturn(Mono.just(PageImpl(list, PageRequest.of(pageNo, pageSize), list.size.toLong())))

    // invoke
    client.get().uri("/accident-register/checked" + (if (status != null) "?status=${status.name}" else ""))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.count").isEqualTo(list.size)
      .jsonPath("$.pageNo").isEqualTo(pageNo)
      .jsonPath("$.pageSize").isEqualTo(pageSize)
      .jsonPath("$.rows[0].code").isEqualTo("20180101_02")
      .jsonPath("$.rows[1].code").isEqualTo("20180101_01")

    // verify
    verify(accidentRegisterService).findChecked(pageNo, pageSize, status, null)
  }
}