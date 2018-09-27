package cn.gftaxi.traffic.accident.rest.webflux.handler.draft

import cn.gftaxi.traffic.accident.common.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.RequestPredicates.contentType
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.context.SystemContext
import tech.simter.reactive.security.ReactiveSecurityService
import tech.simter.reactive.web.Utils.TEXT_PLAIN_UTF8
import java.util.*
import javax.json.Json

/**
 * 上报案件信息 [HandlerFunction]。
 *
 * @author RJ
 */
@Component("cn.gftaxi.traffic.accident.rest.webflux.handler.draft.SubmitHandler")
class SubmitHandler @Autowired constructor(
  private val accidentDraftService: AccidentDraftService,
  private val securityService: ReactiveSecurityService
) : HandlerFunction<ServerResponse> {
  override fun handle(request: ServerRequest): Mono<ServerResponse> {
    return request.bodyToMono<AccidentDraftDto4Form>()
      .flatMap { dto ->
        // 自动设置当前用户信息
        securityService.getAuthenticatedUser()
          .map(Optional<SystemContext.User>::get)
          .map { user ->
            dto.apply {
              authorId = user.account
              authorName = user.name
              source = source ?: "BC"
            }
          }
      }
      .flatMap { dto ->
        accidentDraftService.submit(dto).map {
          Json.createObjectBuilder()
            .add("id", it.first.id!!)
            .add("code", it.first.code)
            .add("draftTime", it.second.draftTime!!.format(FORMAT_DATE_TIME_TO_MINUTE))
            .add("overdueDraft", it.second.overdueDraft!!)
            .build().toString()
        }
      }
      .flatMap { created(request.uri()).contentType(APPLICATION_JSON_UTF8).syncBody(it) }
      // error mapping
      .onErrorResume(PermissionDeniedException::class.java) {
        ServerResponse.status(FORBIDDEN).contentType(TEXT_PLAIN_UTF8).syncBody(it.message ?: "")
      }
      // 指定车号和事发时间的案件已经存在
      .onErrorResume(NonUniqueException::class.java) { badRequest().syncBody(it.message ?: "") }
  }

  companion object {
    /** The default [RequestPredicate] */
    val REQUEST_PREDICATE: RequestPredicate = POST("/accident-draft").and(contentType(APPLICATION_JSON_UTF8))
  }
}