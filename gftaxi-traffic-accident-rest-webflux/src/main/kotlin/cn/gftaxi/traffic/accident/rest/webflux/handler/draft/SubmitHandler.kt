package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.RequestPredicates.contentType
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import javax.json.Json

/**
 * 上报案件信息 [HandlerFunction]。
 *
 * @author RJ
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.draft.SubmitHandler")
class SubmitHandler @Autowired constructor(
  private val accidentDraftService: AccidentDraftService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono<AccidentDraftDto4Submit>()
      .flatMap { dto ->
        accidentDraftService.submit(dto).map {
          Json.createObjectBuilder()
            .add("id", it.first)
            .add("code", it.second)
            .add("createTime", dto.createTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
            .build().toString()
        }
      }
      .flatMap { created(request.uri()).contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // 车号+事发时间重复时
      .onErrorResume(NonUniqueException::class.java) { badRequest().syncBody(it.message ?: "") }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = POST("/accident-draft").and(contentType(APPLICATION_JSON_UTF8))
  }
}