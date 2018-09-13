package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.rest.webflux.UnitTestConfiguration
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.SubmitHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
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
import java.time.OffsetDateTime
import javax.json.Json

/**
 * Test [SubmitHandler]。
 *
 * @author cjw
 * @author RJ
 */
@SpringJUnitConfig(UnitTestConfiguration::class, SubmitHandler::class)
@MockBean(AccidentDraftService::class)
@WebFluxTest
class SubmitHandlerTest @Autowired constructor(
  private val client: WebTestClient,
  private val accidentDraftService: AccidentDraftService
) {
  @Configuration
  class Cfg {
    @Bean
    fun theRoute(handler: SubmitHandler): RouterFunction<ServerResponse> = route(REQUEST_PREDICATE, handler)
  }

  @Test
  fun submit() {
    // mock
    val now = OffsetDateTime.now()
    val dto = AccidentDraftDto4Submit().apply {
      carPlate = "粤A.23J5"
      driverName = "林河"
      happenTime = now
      location = "荔湾区福利路"
      hitForm = "车辆间事故"
      hitType = "追尾碰撞"
      describe = "撞车"
      source = "BC"
      authorName = "韩智勇"
      authorId = "hzy"
      draftTime = now
    }
    val data = Json.createObjectBuilder()
    `data`.add("carPlate", dto.carPlate)
    `data`.add("driverName", dto.driverName)
    `data`.add("happenTime", dto.happenTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
    `data`.add("location", dto.location)
    `data`.add("hitForm", dto.hitForm)
    `data`.add("hitType", dto.hitType)
    `data`.add("describe", dto.describe)
    `data`.add("source", dto.source)
    `data`.add("authorName", dto.authorName)
    `data`.add("authorId", dto.authorId)
    `data`.add("draftTime", dto.draftTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))

    val id = 1
    val code = "20180909_01"
    `when`(accidentDraftService.submit(any())).thenReturn(Mono.just(Pair(id, code)))

    // invoke
    client.post().uri("/accident-draft")
      .header("Content-Type", APPLICATION_JSON_UTF8.toString())
      .syncBody(data.build().toString())
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.id").isEqualTo(id)
      .jsonPath("$.code").isEqualTo(code)

    // verify
    verify(accidentDraftService).submit(any())
  }
}