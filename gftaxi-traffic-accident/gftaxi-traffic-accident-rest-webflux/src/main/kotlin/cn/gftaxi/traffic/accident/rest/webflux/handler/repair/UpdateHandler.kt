package cn.gftaxi.traffic.accident.rest.webflux.handler.repair

import cn.gftaxi.traffic.accident.dto.RepairDto4FormUpdate
import cn.gftaxi.traffic.accident.service.RepairService
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.PATCH
import org.springframework.web.reactive.function.server.RequestPredicates.contentType
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8


/**
 *更新事故维修信息的
 *
 * @author SX
 */
@Component
class UpdateHandler constructor(
  private val service: RepairService
): HandlerFunction<ServerResponse>{
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request
      .bodyToMono<RepairDto4FormUpdate>()
      .flatMap {
        service.update(request.pathVariable("id").toInt(),it)
      }
      .then(noContent().build())
      .onErrorResume(NotFoundException::class.java){
        status(NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?:"")
      }
      .onErrorResume(PermissionDeniedException::class.java){
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    val REQUEST_PREDICATE: RequestPredicate = PATCH("/accident-repair/{id}")
      .and(contentType(APPLICATION_JSON_UTF8))
  }
}