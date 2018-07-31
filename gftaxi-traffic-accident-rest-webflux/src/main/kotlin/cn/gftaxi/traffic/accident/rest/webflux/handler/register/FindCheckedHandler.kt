package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

/**
 * 获取已审核案件信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component
class FindCheckedHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val pageNo = request.queryParam("pageNo").orElse("1").toInt()
    val pageSize = request.queryParam("pageSize").orElse("25").toInt()
    val statusStr = request.queryParam("status")
    val status = if (statusStr.isPresent) Status.valueOf(statusStr.get()) else null
    val search = request.queryParam("search").orElse(null)
    val page = accidentRegisterService.findChecked(pageNo, pageSize, status, search)
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
              "happenTime" to it.happenTime.format(FORMAT_DATE_TIME_TO_MINUTE),
              "checkedResult" to it.checkedResult.name,
              "checkedComment" to it.checkedComment,
              "checkerName" to it.checkerName,
              "checkedCount" to it.checkedCount,
              "checkedTime" to it.checkedTime.format(FORMAT_DATE_TIME_TO_MINUTE),
              "attachmentName" to it.attachmentName,
              "attachmentId" to it.attachmentId
            )
          }.toList()
        )
      }

    return ServerResponse.ok()
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .body(page)
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-register/checked")
  }
}