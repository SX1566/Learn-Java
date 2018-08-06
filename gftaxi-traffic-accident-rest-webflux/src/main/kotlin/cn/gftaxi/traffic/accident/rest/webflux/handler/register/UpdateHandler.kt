package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.rest.webflux.Utils.TEXT_PLAIN_UTF8
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.PATCH
import org.springframework.web.reactive.function.server.RequestPredicates.contentType
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException

/**
 * 更新事故登记信息的 [HandlerFunction]。
 *
 * @author RJ
 */
@Component
class UpdateHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono<Map<String, Any?>>()
      // TODO 将请求体的属性值转换为 PO 的属性值
      .map { it }
      // 执行信息更新
      .flatMap { accidentRegisterService.update(request.pathVariable("id").toInt(), it) }
      // response
      .then(ServerResponse.noContent().build())
      // error mapping
      .onErrorResume(NotFoundException::class.java, {
        ServerResponse.status(NOT_FOUND).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      })
      .onErrorResume(PermissionDeniedException::class.java, {
        ServerResponse.status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      })
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = PATCH("/accident-register/{id}")
      .and(contentType(MediaType.APPLICATION_JSON_UTF8))
  }
}