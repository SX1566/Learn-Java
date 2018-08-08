package cn.gftaxi.webflux.dynamicdto

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
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
      .map {
        println(it)
        it
      }
      // response
      .then(ServerResponse.noContent().build())
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = PATCH("/date-time/{id}")
      .and(contentType(MediaType.APPLICATION_JSON_UTF8))
  }
}