package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
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
    return accidentRegisterService.statSummary().collectList()
      // response
      .flatMap { ServerResponse.ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // error mapping
      .onErrorResume(PermissionDeniedException::class.java, {
        ServerResponse.status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      })
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-register/stat/summary")
  }
}