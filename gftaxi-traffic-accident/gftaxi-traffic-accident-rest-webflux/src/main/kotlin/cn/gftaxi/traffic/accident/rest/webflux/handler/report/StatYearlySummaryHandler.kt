package cn.gftaxi.traffic.accident.rest.webflux.handler.report

import cn.gftaxi.traffic.accident.service.AccidentStatService
import org.springframework.beans.factory.annotation.Autowired
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
import java.time.Year

/**
 * 事故报告年度汇总统计的 [HandlerFunction]。
 *
 * @author zh
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.report.StatYearlySummaryHandler")
class StatYearlySummaryHandler @Autowired constructor(
  private val accidentStatService: AccidentStatService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val currentYear = Year.now()
    val from = request.queryParam("from").map { Year.of(it.toInt()) }.orElse(currentYear.minusYears(1))
    val to = request.queryParam("to").map { Year.of(it.toInt()) }.orElse(currentYear)
    return accidentStatService.statReportYearlySummary(from, to).collectList()
      // response
      .flatMap { ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // error mapping
      .onErrorResume(PermissionDeniedException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-report/stat-yearly-summary")
  }
}