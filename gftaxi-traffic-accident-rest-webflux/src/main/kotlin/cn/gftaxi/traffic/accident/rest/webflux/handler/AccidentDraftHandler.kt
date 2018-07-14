package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

/**
 * 事故报案的 [HandlerFunction]。
 *
 * @author cjw
 */
@Component
class AccidentDraftHandler @Autowired constructor(
  private val accidentDraftService: AccidentDraftService
) {
  fun find(request: ServerRequest): Mono<ServerResponse> {
    val pageNo = request.queryParam("pageNo").orElse("1").toInt()
    val pageSize = request.queryParam("pageSize").orElse("25").toInt()
    val status = AccidentDraft.Status.valueOf(request.queryParam("status").orElse(AccidentDraft.Status.Todo.name))
    val search = request.queryParam("search").orElse(null)
    return ServerResponse.ok().body(
      accidentDraftService.find(pageNo.minus(1), pageSize, status, search).map {
        hashMapOf(
          "count" to it.count(),
          "pageNo" to it.pageable.pageNumber,
          "pageSize" to it.pageable.pageSize,
          "rows" to it.content
        )
      }
    )
  }

  fun get(request: ServerRequest): Mono<ServerResponse> {
    return accidentDraftService.get(request.pathVariable("code")).flatMap {
      ServerResponse.ok().contentType(APPLICATION_JSON_UTF8).syncBody(it)
    }
  }

  companion object {
    val FIND_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-draft")
      .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON_UTF8))
    val GET_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-draft/{code}")
      .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON_UTF8))
  }
}