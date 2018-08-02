package cn.gftaxi.traffic.accident.rest.webflux.handler.register

import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import javax.naming.NameNotFoundException

/**
 * 获取指定编号的登记信息的 [HandlerFunction]。
 *
 * @author JF
 * @author RJ
 */
@Component
class GetByCodeHandler @Autowired constructor(
  private val accidentRegisterService: AccidentRegisterService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    NameNotFoundException()
    ChangeSetPersister.NotFoundException()
    ClassNotFoundException()
    return accidentRegisterService.getByCode(request.pathVariable("code"))
      .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(it) }
      .onErrorResume(NotFoundException::class.java, { ServerResponse.notFound().build() })
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = RequestPredicates.GET("/accident-register/code/{code}")
  }
}