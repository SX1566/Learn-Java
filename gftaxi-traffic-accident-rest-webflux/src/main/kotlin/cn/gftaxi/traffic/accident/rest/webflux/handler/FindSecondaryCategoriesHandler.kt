package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.service.AccidentCategoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

/**
 * 获取指定一级分类下的二级分类列表（按一级分类的编码）的 [HandlerFunction]。
 *
 * @author JF
 */
@Component
class FindSecondaryCategoriesHandler @Autowired constructor(
  private val accidentCategoryService: AccidentCategoryService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val primaryCategorySn = request.pathVariable("sn")
    val includeDisabled = request.queryParam("include-disabled")
    val includeDisabledStatus = includeDisabled.isPresent && "true" == includeDisabled.get().toLowerCase()

    return ok()
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .body(
        accidentCategoryService
          .findSecondaryCategories(includeDisabledStatus, primaryCategorySn)
          .map {
            val result = hashMapOf(
              "sn" to it.sn,
              "name" to it.name
            )
            if (includeDisabledStatus) result["stage"] = it.status.name
            result
          }
      )
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/category/{sn}/children")
  }
}