package cn.gftaxi.webflux.dynamicdto

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * @author RJ
 */
@Component
class GetHandler @Autowired constructor() : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return Mono.just(
      DynamicDto(mutableMapOf(
        "name" to "test",
        "offsetDateTime" to OffsetDateTime.of(2018, 10, 1, 8, 30, 20, 0, OffsetDateTime.now().offset)
      ).withDefault { null })
    ).flatMap {
      ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(it)
    }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/date-time/{id}")
  }
}