package cn.gftaxi.traffic.accident.rest.webflux.handler.report

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.convert
import cn.gftaxi.traffic.accident.service.AccidentReportService
import org.springframework.http.HttpStatus.FORBIDDEN
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
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 * 事故报告视图的 [HandlerFunction]。
 *
 * @author zh
 * @author RJ
 */
@Component
class FindHandler constructor(
  private val service: AccidentReportService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val pageNo = request.queryParam("pageNo").map { it.toInt() }.orElse(1)
    val pageSize = request.queryParam("pageSize").map { it.toInt() }.orElse(25)
    val reportStatuses = request.queryParam("status")
      .map { str -> str.split(",").map { AuditStatus.valueOf(it) } }.orElse(null)
    val search = request.queryParam("search").orElse(null)
    return service.find(pageNo = pageNo, pageSize = pageSize, reportStatuses = reportStatuses, search = search)
      .map { it.convert() }
      // response
      .flatMap { ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      .onErrorResume(PermissionDeniedException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-report")
  }
}