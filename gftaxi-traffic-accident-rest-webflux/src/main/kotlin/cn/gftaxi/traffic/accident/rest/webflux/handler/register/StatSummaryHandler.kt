package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.ScopeType
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import tech.simter.exception.PermissionDeniedException

/**
 * 事故登记汇总统计的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component
class StatSummaryHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val scopeType = ScopeType.valueOf(uppercaseFirstChar(request.pathVariable("scopeType")))
    val from = request.queryParam("from").orElse(null).toInt()
    val to = request.queryParam("to").orElse(null).toInt()
    return accidentRegisterService.statSummary(scopeType, from, to).collectList()
      // response
      .flatMap { ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // error mapping
      .onErrorResume(PermissionDeniedException::class.java, {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      })
  }

  // 字符串首字母大写
  private fun uppercaseFirstChar(str: String): String {
    return "${str.substring(0, 1).toUpperCase()}${str.substring(1)}"
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate =
      RequestPredicates.GET("/accident-register/stat/summary/{scopeType}")
  }
}