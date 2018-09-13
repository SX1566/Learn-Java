package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Update
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.rest.webflux.Utils
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import tech.simter.exception.NotFoundException
import javax.json.Json

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
    return ok().body(
      accidentDraftService.find(pageNo, pageSize, status, search).map {
        hashMapOf(
          "count" to it.count(),
          "pageNo" to it.pageable.pageNumber,
          "pageSize" to it.pageable.pageSize,
          "rows" to it.content.map {
            mapOf(
              "id" to it.id,
              "code" to it.code,
              "status" to it.status.name,
              "motorcadeName" to it.motorcadeName,
              "carPlate" to it.carPlate,
              "driverName" to it.driverName,
              "happenTime" to it.happenTime.format(FORMAT_DATE_TIME_TO_MINUTE),
              "reportTime" to it.reportTime.format(FORMAT_DATE_TIME_TO_MINUTE),
              "location" to it.location,
              "hitForm" to it.hitForm,
              "hitType" to it.hitType,
              "overdue" to it.overdue,
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

  fun get(request: ServerRequest): Mono<ServerResponse> {
    return accidentDraftService.get(request.pathVariable("id").toInt()).flatMap {
      ok().contentType(APPLICATION_JSON_UTF8).syncBody(
        mapOf(
          "id" to it.id,
          "code" to it.code,
          "status" to it.status.name,
          "motorcadeName" to it.motorcadeName,
          "carPlate" to it.carPlate,
          "driverName" to it.driverName,
          "happenTime" to it.happenTime.format(FORMAT_DATE_TIME_TO_MINUTE),
          "reportTime" to it.reportTime.format(FORMAT_DATE_TIME_TO_MINUTE),
          "location" to it.location,
          "hitForm" to it.hitForm,
          "hitType" to it.hitType,
          "overdue" to it.overdue,
          "source" to it.source,
          "authorName" to it.authorName,
          "authorId" to it.authorId,
          "describe" to it.describe
        )
      )
    }
  }

  fun submit(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono<AccidentDraftDto4Submit>()
      .flatMap { dto ->
        accidentDraftService.submit(dto).map {
          Json.createObjectBuilder()
            .add("id", it.first)
            .add("code", it.second)
            .add("reportTime", dto.reportTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
            .build().toString()
        }
      }
      .flatMap { created(request.uri()).contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(it) }
      // 车号+事发时间重复时
      .onErrorResume(NonUniqueException::class.java) { badRequest().syncBody(it.message ?: "") }
  }

  fun update(request: ServerRequest): Mono<ServerResponse> {
    return request
      .bodyToMono<AccidentDraftDto4Update>()
      .flatMap {
        accidentDraftService.modify(request.pathVariable("id").toInt(), it.data.map)
      }
      .then(noContent().build())
      // 找不到 id 对应的资源
      .onErrorResume(NotFoundException::class.java) {
        status(NOT_FOUND).contentType(Utils.TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    val FIND_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-draft")
    val GET_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-draft/{id}")
    val SUBMIT_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.POST("/accident-draft")
      .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON_UTF8))
    val UPDATE_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.PATCH("/accident-draft/{id}")
      .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON_UTF8))
  }
}