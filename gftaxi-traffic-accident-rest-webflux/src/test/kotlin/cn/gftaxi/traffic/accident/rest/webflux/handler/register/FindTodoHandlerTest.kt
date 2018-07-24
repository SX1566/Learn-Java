package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.ToCheck
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.FindTodoHandler.Companion.REQUEST_PREDICATE
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
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Flux
import java.time.OffsetDateTime

/**
 * Test [FindTodoHandler]。
 *
 * @author RJ
 */
@SpringJUnitConfig(FindTodoHandler::class)
@EnableWebFlux
@MockBean(AccidentRegisterService::class)
class FindTodoHandlerTest @Autowired constructor(
  handler: FindTodoHandler,
  private val accidentRegisterService: AccidentRegisterService
) {
  private val client = bindToRouterFunction(RouterFunctions.route(REQUEST_PREDICATE, handler)).build()
  private fun randomDto(code: String): AccidentRegisterDto4Todo {
    val now = OffsetDateTime.now()
    return AccidentRegisterDto4Todo(code = code,
      carPlate = "粤A.00001",
      driverName = "driver1",
      outsideDriver = false,
      happenTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      hitForm = "车辆间事故",
      hitType = "追尾碰撞",
      location = "芳村上市路",
      authorName = "小明",
      authorId = "Ming",
      reportTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      overdueReport = false,
      registerTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset),
      overdueRegister = false,
      submitTime = OffsetDateTime.of(2018, 1, 1, 10, 30, 0, 0, now.offset)
    )
  }

  @Test
  fun findBoth() {
    findByStatus(null)
  }

  @Test
  fun findDraftOnly() {
    findByStatus(Draft)
  }

  @Test
  fun findToCheckOnly() {
    findByStatus(ToCheck)
  }

  private fun findByStatus(status: Status?) {
    var code = 1
    // mock
    val dto = randomDto(code = "20180101_0$code")
    `when`(accidentRegisterService.findTodo(status))
      .thenReturn(Flux.just(dto.copy(code = "20180101_0${++code}"), dto))

    // invoke
    client.get().uri("/accident-register/todo" + (if (status != null) "?status=${status.name}" else ""))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.[0].code").isEqualTo("20180101_02")
      .jsonPath("$.[1].code").isEqualTo("20180101_01")

    // verify
    verify(accidentRegisterService).findTodo(status)
  }
}