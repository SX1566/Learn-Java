package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.dto.SecondaryCategoryDto
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindSecondaryCategoriesHandler.Companion.REQUEST_PREDICATE
import cn.gftaxi.traffic.accident.service.AccidentCategoryService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import reactor.core.publisher.Flux
import tech.simter.category.po.Category

/**
 * 测试获取指定一级分类下的二级分类列表（按一级分类的编码）的 [HandlerFunction]。
 *
 * @author JF
 * @author RJ
 */
@SpringJUnitConfig(FindSecondaryCategoriesHandler::class)
@MockBean(AccidentCategoryService::class)
class FindSecondaryCategoriesHandlerTest @Autowired constructor(
  private val accidentCategoryService: AccidentCategoryService,
  handler: FindSecondaryCategoriesHandler
) {
  private val client = bindToRouterFunction(RouterFunctions.route(REQUEST_PREDICATE, handler)).build()

  @Test
  fun findWithIncludeDisabled() {
    // mock
    val sn = "10"
    val belong = "SGZR"
    val name = "全部责任"
    val status = Category.Status.Enabled
    `when`(accidentCategoryService.findSecondaryCategories(true, belong))
      .thenReturn(Flux.just(SecondaryCategoryDto(belong, sn, name, status)))

    // invoke
    client.get().uri("/category/$belong/children?include-disabled=true")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.[0].sn").isEqualTo(sn)
      .jsonPath("$.[0].name").isEqualTo(name)
      .jsonPath("$.[0].stage").isEqualTo(status.name)

    // verify
    verify(accidentCategoryService).findSecondaryCategories(true, belong)
  }

  @Test
  fun findWithoutIncludeDisabled() {
    // mock
    val sn = "10"
    val belong = "SGZR"
    val name = "全部责任"
    val status = Category.Status.Enabled
    `when`(accidentCategoryService.findSecondaryCategories(false, belong))
      .thenReturn(Flux.just(SecondaryCategoryDto(belong, sn, name, status)))

    // invoke
    client.get().uri("/category/$belong/children")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.[0].sn").isEqualTo(sn)
      .jsonPath("$.[0].name").isEqualTo(name)
      .jsonPath("$.[0].stage").doesNotExist()

    // verify
    verify(accidentCategoryService).findSecondaryCategories(false, belong)
  }
}