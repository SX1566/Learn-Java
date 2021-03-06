package cn.gftaxi.traffic.accident.rest.webflux.handler.repair

import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import cn.gftaxi.traffic.accident.service.RepairService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8

/**
 *审核事故维修信息的
 *
 * @author SX
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.repair.CheckedHandler")
class CheckHandler @Autowired constructor(
  private  val repairService: RepairService
) : HandlerFunction<ServerResponse>{
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request
      // 将请求体的 json 转换为 DTO
      .bodyToMono<CheckedInfoDto>()
      // 执行审核处理
      .flatMap { repairService.checked(request.pathVariable("id").toInt(), it) }
      // response
      .then(ServerResponse.noContent().build())
      // error mapping
      .onErrorResume(NotFoundException::class.java) {
        ServerResponse.status(HttpStatus.NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(ForbiddenException::class.java) {
        ServerResponse.status(HttpStatus.FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(PermissionDeniedException::class.java) {
        ServerResponse.status(HttpStatus.FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

    companion object {
      /** The default [RequestPredicate] */
      val REQUEST_PREDICATE: RequestPredicate = POST("/accident-repair/checked/{id}")
        .and(RequestPredicates.contentType(APPLICATION_JSON_UTF8))
    }


}