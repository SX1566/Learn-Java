package cn.gftaxi.traffic.accident.rest.webflux.handler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.simter.category.po.Category
import tech.simter.category.service.CategoryService
import java.util.stream.Collectors.groupingBy

/**
 * 获取按一级分类编码分组的所有二级分类列表的 [HandlerFunction]。
 *
 * @author JF
 */
@Component
class FindAllSecondaryCategoriesHandler @Autowired constructor(
  private val categoryService: CategoryService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val includeDisabled = request.queryParam("include-disabled")
    val childStatuses =
      if (includeDisabled.isPresent && "true" == includeDisabled.get().toLowerCase()) {
        arrayOf(Category.Status.Enabled, Category.Status.Disabled)
      } else arrayOf(Category.Status.Enabled)

    return ok()
      .contentType(APPLICATION_JSON_UTF8)
      .body(
        categoryService.findChild(1, arrayOf("JTSG_CATEGORY"), arrayOf(Category.Status.Enabled))
          .map { it.sn }
          .collectList()
          .flatMapMany { categoryService.findChild(2, it.toTypedArray(), childStatuses) }
          .collectList()
          .map { it.stream().collect(groupingBy(Category::pid)) }
          .map {
            val keys = it.keys.toList()
            val result = HashMap<String, List<String>>()
            keys.forEach { key ->
              result[key!!.sn] = it[key]!!.map { it.name }
            }
            result
          }
      )
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/category/group")
  }
}