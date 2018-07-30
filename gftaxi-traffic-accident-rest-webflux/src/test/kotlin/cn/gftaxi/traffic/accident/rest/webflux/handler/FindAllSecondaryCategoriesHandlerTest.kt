package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.rest.webflux.handler.FindAllSecondaryCategoriesHandler.Companion.REQUEST_PREDICATE
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
import tech.simter.category.service.CategoryService

/**
 * 测试获取按一级分类编码分组的所有二级分类列表的 [HandlerFunction]。
 *
 * @author JF
 */
@SpringJUnitConfig(FindAllSecondaryCategoriesHandler::class)
@MockBean(CategoryService::class)
class FindAllSecondaryCategoriesHandlerTest @Autowired constructor(
  private val categoryService: CategoryService,
  handler: FindAllSecondaryCategoriesHandler
) {
  private val client = bindToRouterFunction(RouterFunctions.route(REQUEST_PREDICATE, handler)).build()

  @Test
  fun findWithIncludeDisabled() {
    // mock
    val sgxz = Category(null, null, Category.Status.Enabled, "事故性质", "SGXZ")
    val ccsssg = Category(null, sgxz, Category.Status.Enabled, "财产损失事故", "CCSSSG")
    val srsg = Category(null, sgxz, Category.Status.Enabled, "伤人事故", "SRSG")
    val sgxt = Category(null, null, Category.Status.Enabled, "事故形态", "SGXT")
    val cljsg = Category(null, sgxt, Category.Status.Disabled, "车辆间事故", "CLJSG")

    `when`(categoryService.findChild(1, arrayOf("JTSG_CATEGORY"), arrayOf(Category.Status.Enabled)))
      .thenReturn(Flux.just(sgxt, sgxz))
    `when`(categoryService.findChild(2, arrayOf(sgxt.sn, sgxz.sn), arrayOf(Category.Status.Enabled, Category.Status.Disabled)))
      .thenReturn(Flux.just(cljsg, ccsssg, srsg))

    // invoke
    client.get().uri("/category/group?include-disabled=true")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.SGXT[0]").isEqualTo(cljsg.name)
      .jsonPath("$.SGXZ[0]").isEqualTo(ccsssg.name)
      .jsonPath("$.SGXZ[1]").isEqualTo(srsg.name)

    // verify
    verify(categoryService).findChild(1, arrayOf("JTSG_CATEGORY"), arrayOf(Category.Status.Enabled))
    verify(categoryService).findChild(2, arrayOf(sgxt.sn, sgxz.sn), arrayOf(Category.Status.Enabled, Category.Status.Disabled))
  }

  @Test
  fun findWithoutIncludeDisabled() {
    // mock
    val sgxz = Category(null, null, Category.Status.Enabled, "事故性质", "SGXZ")
    val ccsssg = Category(null, sgxz, Category.Status.Enabled, "财产损失事故", "CCSSSG")
    val srsg = Category(null, sgxz, Category.Status.Enabled, "伤人事故", "SRSG")

    `when`(categoryService.findChild(1, arrayOf("JTSG_CATEGORY"), arrayOf(Category.Status.Enabled)))
      .thenReturn(Flux.just(sgxz))
    `when`(categoryService.findChild(2, arrayOf(sgxz.sn), arrayOf(Category.Status.Enabled)))
      .thenReturn(Flux.just(srsg, ccsssg))

    // invoke
    client.get().uri("/category/group")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
      .expectBody()
      .jsonPath("$.SGXZ[0]").isEqualTo(srsg.name)
      .jsonPath("$.SGXZ[1]").isEqualTo(ccsssg.name)

    // verify
    verify(categoryService).findChild(1, arrayOf("JTSG_CATEGORY"), arrayOf(Category.Status.Enabled))
    verify(categoryService).findChild(2, arrayOf(sgxz.sn), arrayOf(Category.Status.Enabled))
  }
}