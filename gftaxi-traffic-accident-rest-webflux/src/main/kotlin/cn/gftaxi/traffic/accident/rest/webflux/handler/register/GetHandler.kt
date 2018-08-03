package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException

/**
 * 获取指定 ID 事故登记信息的 [HandlerFunction]。
 *
 * @author JF
 * @author RJ
 */
@Component
class GetHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return accidentRegisterService.get(request.pathVariable("id").toInt())
      .flatMap {
        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(it)
      }
      .onErrorResume(NotFoundException::class.java, {
        ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(it.message ?: "")
      })
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-register/{id}")
  }
}