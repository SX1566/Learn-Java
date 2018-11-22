package cn.gftaxi.traffic.accident.rest.webflux.handler.repair

import cn.gftaxi.traffic.accident.common.MaintainStatus
import cn.gftaxi.traffic.accident.common.convert
import cn.gftaxi.traffic.accident.service.RepairService
import netscape.security.Privilege.FORBIDDEN
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.PermissionDeniedDataAccessException
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicate
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 *获取事故维修信息的[HandlerFunction]。
 *
 * @author SX
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.repair.FindHandler")
class FindHandler @Autowired constructor(
  private val repairService: RepairService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    val pageNo = request.queryParam("pageNo").orElse("1").toInt()
    val pageSize = request.queryParam("pageSize").orElse("25").toInt()
    val repairStatuses = request.queryParam("status")
      .map { v -> v.split(",").map { MaintainStatus.valueOf(it) } }
      .orElse(null)
    val search = request.queryParam("search").orElse(null)

    return repairService.find(pageNo, pageSize, repairStatuses, search)
      .map { it.convert() }
      // response
      .flatMap { ServerResponse.ok().contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      //error mapping
      .onErrorResume(PermissionDeniedDataAccessException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = GET("/accident-repair")
  }

}