package cn.gftaxi.webflux.dynamicdto

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.PATCH
import org.springframework.web.reactive.function.server.RequestPredicates.contentType
import reactor.core.publisher.Mono

/**
 * @author RJ
 */
@Component
class PatchHandler @Autowired constructor() : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono<DynamicDto>()
      .flatMap {
        println(it)
        ServerResponse.ok().contentType(APPLICATION_JSON_UTF8).syncBody(it)
      }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = PATCH("/").and(contentType(APPLICATION_JSON_UTF8))
  }
}