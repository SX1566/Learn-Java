package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.dto.CheckedInfo
import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException

/**
 * 审核事故登记信息的 [CheckedHandler]。
 *
 * @author RJ
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.register.CheckedHandler")
class CheckedHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request
      // 将请求体的 json 转换为 DTO
      .bodyToMono<CheckedInfo>()
      // 执行审核处理
      .flatMap { accidentRegisterService.checked(request.pathVariable("id").toInt(), it) }
      // response
      .then(noContent().build())
      // error mapping
      .onErrorResume(NotFoundException::class.java) {
        status(NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(ForbiddenException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      .onErrorResume(PermissionDeniedException::class.java) {
        status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = POST("/accident-register/checked/{id}")
      .and(RequestPredicates.contentType(APPLICATION_JSON_UTF8))
  }
}