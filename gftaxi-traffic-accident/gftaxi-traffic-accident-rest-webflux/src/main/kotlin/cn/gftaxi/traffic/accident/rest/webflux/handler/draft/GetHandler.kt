package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * 获取指定 ID 事故报案信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.draft.GetHandler")
class GetHandler @Autowired constructor(
  private val accidentDraftService: AccidentDraftService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val id = request.pathVariable("id").toInt()
    return accidentDraftService.get(id)
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // response
      .flatMap { ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // error mapping
      .onErrorResume(NotFoundException::class.java) {
        status(NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(PermissionDeniedException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-draft/{id}")
  }
}