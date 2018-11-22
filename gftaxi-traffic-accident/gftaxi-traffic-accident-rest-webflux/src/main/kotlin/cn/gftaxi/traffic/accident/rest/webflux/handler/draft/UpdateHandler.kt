package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormUpdate
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.PATCH
import org.springframework.web.reactive.function.server.RequestPredicates.contentType
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * 更新事故报案信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.draft.UpdateHandler")
class UpdateHandler @Autowired constructor(
  private val accidentDraftService: AccidentDraftService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request
      .bodyToMono<AccidentDraftDto4FormUpdate>()
      .flatMap {
        accidentDraftService.update(request.pathVariable("id").toInt(), it)
      }
      .then(ServerResponse.noContent().build())
      // error mapping
      // 案件不存在
      .onErrorResume(NotFoundException::class.java) {
        status(NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(PermissionDeniedException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = PATCH("/accident-draft/{id}")
      .and(contentType(APPLICATION_JSON_UTF8))
  }
}