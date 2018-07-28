package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import java.time.format.DateTimeFormatter

/**
 * 获取待登记、待审核案件信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component
class FindTodoHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val statusStr = request.queryParam("status")
    val status = if (statusStr.isPresent) Status.valueOf(statusStr.get()) else null
    val publisher = accidentRegisterService.findTodo(status)
      .map {
        mapOf(
          "id" to it.id,
          "code" to it.code,
          "carPlate" to it.carPlate,
          "driverName" to it.driverName,
          "driverType" to it.driverType.name,
          "happenTime" to it.happenTime.format(formatter),
          "authorName" to it.authorName,
          "authorId" to it.authorId,
          "hitForm" to it.hitForm,
          "hitType" to it.hitType,
          "location" to it.location,
          "reportTime" to it.reportTime.format(formatter),
          "overdueReport" to it.overdueReport,
          "registerTime" to it.registerTime?.format(formatter),
          "overdueRegister" to it.overdueRegister,
          "submitTime" to it.submitTime?.format(formatter)
        )
      }
    return ServerResponse.ok()
      .contentType(MediaType.APPLICATION_JSON_UTF8)
      .body(publisher)
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-register/todo")
  }
}