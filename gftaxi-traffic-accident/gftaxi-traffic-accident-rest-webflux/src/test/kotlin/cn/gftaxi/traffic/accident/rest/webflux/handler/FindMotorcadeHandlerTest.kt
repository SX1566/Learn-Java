package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.bc.dto.MotorcadeDto
import cn.gftaxi.traffic.accident.common.MotorcadeStatus
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindMotorcadeHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentCommonService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.toFlux
import kotlin.test.assertEquals

/**
 * 测试获取所属车队信息列表的的 [HandlerFunction]。
 *
 * @author jw
 */
@SpringJUnitConfig(FindMotorcadeHandler::class)
@MockBean(AccidentCommonService::class)
class FindMotorcadeHandlerTest @Autowired constructor(
  private val accidentCommonService: AccidentCommonService,
  handler: FindMotorcadeHandler
) {
  private val client = bindToRouterFunction(RouterFunctions.route(REQUEST_PREDICATE, handler)).build()

  @Test
  fun findWithIncludeDisabled() {
    find(true)
  }

  @Test
  fun findWithoutIncludeDisabled() {
    find(false)
  }

  private fun find(isIncludeDisabled: Boolean) {
    // mock
    val expect = arrayListOf<MotorcadeDto>().apply {
      for (i in 0..size) {
        val status = if (isIncludeDisabled) {
          // 判断奇偶数来生成不一样的车队状态
          if (0 == i % 2) MotorcadeStatus.Enabled
          else MotorcadeStatus.Disabled
        } else MotorcadeStatus.Enabled
        add(MotorcadeDto(id = i, name = "一分${i + 1}队", sn = i, status = status, branchId = 1,
          branchName = "一分公司", captainId = Math.random().toInt(), captainName = "captain#$i"))
      }
    }
    `when`(accidentCommonService.findMotorcade(isIncludeDisabled)).thenReturn(expect.toFlux())

    // invoke
    client.get().uri("/motorcade?include-disabled=$isIncludeDisabled")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBodyList(MotorcadeDto::class.java)
      .consumeWith<WebTestClient.ListBodySpec<MotorcadeDto>> {
        val actual = it.responseBody
        assertEquals(actual!!.isNotEmpty(), true)
        assertEquals(actual.size, expect.size)
        for (i in 0 until actual.size)
          assertEquals(actual[i], expect[i])
      }

    // verify
    verify(accidentCommonService).findMotorcade(isIncludeDisabled)
  }
}