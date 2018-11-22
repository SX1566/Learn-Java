package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.service.AccidentCommonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import reactor.core.publisher.Mono

/**
 * 获取所属车队信息列表的 [HandlerFunction]。
 *
 * @author jw
 */
@Component
class FindMotorcadeHandler @Autowired constructor(
  private val accidentCommonService: AccidentCommonService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val includeDisabled = request.queryParam("include-disabled").orElse("false")!!.toBoolean()
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(
      accidentCommonService.findMotorcade(includeDisabled)
    )
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/motorcade")
  }
}