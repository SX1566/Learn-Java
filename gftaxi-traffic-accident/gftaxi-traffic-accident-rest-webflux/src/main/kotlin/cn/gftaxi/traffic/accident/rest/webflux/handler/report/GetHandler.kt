package cn.gftaxi.traffic.accident.rest.webflux.handler.report

import cn.gftaxi.traffic.accident.service.AccidentReportService
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
 * 获取指定ID的事故报告信息的 [HandlerFunction]。
 *
 * @author zh
 */
@Component
class GetHandler constructor(
  private val service: AccidentReportService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val id = request.pathVariable("id").toInt()
    return service.get(id)
      .flatMap { ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      .onErrorResume(NotFoundException::class.java) {
        status(NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(PermissionDeniedException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-report/{id}")
  }
}