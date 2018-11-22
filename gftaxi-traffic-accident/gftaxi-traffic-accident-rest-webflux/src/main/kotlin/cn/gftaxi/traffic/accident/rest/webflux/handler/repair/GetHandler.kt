package cn.gftaxi.traffic.accident.rest.webflux.handler.repair

import cn.gftaxi.traffic.accident.service.RepairService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils

/**
 * 获取指定 ID 事故维修信息的
 *
 * @author SX
 */
@Component
class GetHandler constructor(
  private val service: RepairService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val id = request.pathVariable("id").toInt()
    return service.get(id)
      .flatMap { ServerResponse.ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      .onErrorResume(NotFoundException::class.java) {
        ServerResponse.status(HttpStatus.NOT_FOUND).contentType(Utils.TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(PermissionDeniedException::class.java) {
        ServerResponse.status(HttpStatus.FORBIDDEN).contentType(Utils.TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-repair/{id}")
  }

}
