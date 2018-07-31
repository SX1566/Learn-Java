package cn.gftaxi.traffic.accident.rest.webflux.handler

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
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
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
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
    return ServerResponse.ok().body(
      accidentDraftService.find(pageNo, pageSize, status, search).map {
        hashMapOf(
          "count" to it.count(),
          "pageNo" to it.pageable.pageNumber,
          "pageSize" to it.pageable.pageSize,
          "rows" to it.content.map {
            mapOf(
              "code" to it.code,
              "status" to it.status.name,
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
    return accidentDraftService.get(request.pathVariable("code")).flatMap {
      ServerResponse.ok().contentType(APPLICATION_JSON_UTF8).syncBody(
        mapOf(
          "code" to it.code,
          "status" to it.status.name,
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
    return request.bodyToMono<Map<String, String>>()
      .flatMap {
        val dto = AccidentDraftDto4Submit(
          it["carPlate"]!!, it["driverName"]!!, toOffsetDateTime(it["happenTime"]!!), it["location"]!!,
          it["hitForm"]!!, it["hitType"]!!, if (it.size == 10) it["describe"]!! else "", it["source"]!!,
          it["authorName"]!!, it["authorId"]!!, OffsetDateTime.now()
        )
        ServerResponse.created(request.uri()).contentType(MediaType.APPLICATION_JSON_UTF8)
          .body(
            accidentDraftService.submit(dto).map {
              val body = Json.createObjectBuilder()
              body.add("code", it)
              body.add("reportTime", dto.reportTime.format(FORMAT_DATE_TIME_TO_MINUTE))
              body.build().toString()
            })
      }
  }

  fun update(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono<Map<String, String>>()
      .flatMap {
        accidentDraftService
          .modify(
            request.pathVariable("code"),
            AccidentDraftDto4Modify(
              it["carPlate"]!!, it["driverName"]!!, toOffsetDateTime(it["happenTime"]!!), it["location"]!!,
              it["hitForm"]!!, it["hitType"]!!, if (it.size == 7) it["describe"]!! else ""
            )
          )
      }
      .then(ServerResponse.noContent().build())
  }

  /** 时间字符串转 OffsetDateTime 类型 */
  private fun toOffsetDateTime(dateTime: String): OffsetDateTime {
    return OffsetDateTime.of(
      LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
      OffsetDateTime.now().offset
    )
  }

  companion object {
    val FIND_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-draft")
    val GET_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-draft/{code}")
    val SUBMIT_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.POST("/accident-draft")
      .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON_UTF8))
    val UPDATE_REQUEST_PREDICATE: RequestPredicate = RequestPredicates.PUT("/accident-draft/{code}")
      .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON_UTF8))
  }
}