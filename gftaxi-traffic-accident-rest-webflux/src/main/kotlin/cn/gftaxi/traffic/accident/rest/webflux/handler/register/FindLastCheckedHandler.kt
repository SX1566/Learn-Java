package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
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
import tech.simter.exception.ForbiddenException
import tech.simter.exception.PermissionDeniedException

/**
 * 获取已审核案件信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component
class FindLastCheckedHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val pageNo = request.queryParam("pageNo").orElse("1").toInt()
    val pageSize = request.queryParam("pageSize").orElse("25").toInt()
    val statusStr = request.queryParam("status")
    val status = if (statusStr.isPresent) Status.valueOf(statusStr.get()) else null
    val search = request.queryParam("search").orElse(null)

    return accidentRegisterService.findLastChecked(pageNo, pageSize, status, search)
      .map {
        mapOf(
          "count" to it.count(),
          "pageNo" to it.pageable.pageNumber,
          "pageSize" to it.pageable.pageSize,
          "rows" to it.content.map {
            mapOf(
              "id" to it.id,
              "code" to it.code,
              "carPlate" to it.carPlate,
              "driverName" to it.driverName,
              "driverType" to it.driverType.name,
              "location" to it.location,
              "motorcadeName" to it.motorcadeName,
              "happenTime" to it.happenTime.format(FORMAT_DATE_TIME_TO_MINUTE),
              "checkedComment" to it.checkedComment,
              "checkerName" to it.checkerName,
              "checkedCount" to it.checkedCount,
              "checkedTime" to it.checkedTime.format(FORMAT_DATE_TIME_TO_MINUTE),
              "attachmentId" to it.attachmentId
            )
          }.toList()
        )
      }
      // response
      .flatMap { ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // error mapping
      .onErrorResume(ForbiddenException::class.java, {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      })
      .onErrorResume(PermissionDeniedException::class.java, {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      })
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-register/last-checked")
  }
}