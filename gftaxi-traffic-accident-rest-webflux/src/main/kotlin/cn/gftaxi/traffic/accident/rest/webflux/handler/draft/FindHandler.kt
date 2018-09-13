package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.Utils
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

/**
 * 获取事故报案视图信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.draft.FindHandler")
class FindHandler @Autowired constructor(
  private val accidentDraftService: AccidentDraftService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val pageNo = request.queryParam("pageNo").orElse("1").toInt()
    val pageSize = request.queryParam("pageSize").orElse("25").toInt()
    val status = Status.valueOf(request.queryParam("status").orElse(Status.Todo.name))
    val search = request.queryParam("search").orElse(null)
    return ok().body(
      accidentDraftService.find(pageNo, pageSize, status, search).map { page ->
        hashMapOf(
          "count" to page.count(),
          "pageNo" to page.pageable.pageNumber,
          "pageSize" to page.pageable.pageSize,
          "rows" to page.content.map {
            mapOf(
              "id" to it.id,
              "code" to it.code,
              "status" to it.status.name,
              "motorcadeName" to it.motorcadeName,
              "carPlate" to it.carPlate,
              "driverName" to it.driverName,
              "happenTime" to it.happenTime.format(Utils.FORMAT_DATE_TIME_TO_MINUTE),
              "createTime" to it.createTime.format(Utils.FORMAT_DATE_TIME_TO_MINUTE),
              "location" to it.location,
              "hitForm" to it.hitForm,
              "hitType" to it.hitType,
              "overdueCreate" to it.overdueCreate,
              "source" to it.source,
              "authorName" to it.authorName,
              "authorId" to it.authorId,
              "describe" to it.describe
            )
          }.toList()
        )
      }
    )
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-draft")
  }
}