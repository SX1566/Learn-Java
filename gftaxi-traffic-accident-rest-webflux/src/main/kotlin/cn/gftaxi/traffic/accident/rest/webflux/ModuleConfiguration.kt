package cn.gftaxi.traffic.accident.rest.webflux

import cn.gftaxi.traffic.accident.rest.webflux.handler.FindAllSecondaryCategoriesHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindSecondaryCategoriesHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.FindHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.FindHandler as DraftFindHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.GetHandler as DraftGetHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.SubmitHandler as DraftSubmitHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.UpdateHandler as DraftUpdateHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.CheckedHandler as RegisterCheckedHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.FindLastCheckedHandler as RegisterFindLastCheckedHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.FindTodoHandler as RegisterFindTodoHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.GetHandler as RegisterGetHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatSummaryHandler as RegisterStatSummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.ToCheckHandler as RegisterToCheckHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.UpdateHandler as RegisterUpdateHandler

private const val MODULE = "cn.gftaxi.traffic.accident.rest.webflux"

/**
 * All configuration for this module.
 *
 * Register a `RouterFunction<ServerResponse>` with all routers for this module.
 * The default context-path of this router is '/'. And can be config by property `gftaxi.rest.context-path.traffic-accident`.
 *
 * @author JF
 * @author RJ
 */
@Configuration("$MODULE.ModuleConfiguration")
@ComponentScan(MODULE)
class ModuleConfiguration @Autowired constructor(
  @Value("\${module.version.gftaxi-traffic-accident:UNKNOWN}") private val version: String,
  @Value("\${module.rest-context-path.gftaxi-traffic-accident:/accident}") private val contextPath: String,
  private val findSecondaryCategoriesHandler: FindSecondaryCategoriesHandler,
  private val draftGetHandler: DraftGetHandler,
  private val draftFindHandler: DraftFindHandler,
  private val draftSubmitHandler: DraftSubmitHandler,
  private val draftUpdateHandler: DraftUpdateHandler,
  private val registerStatSummaryHandler: RegisterStatSummaryHandler,
  private val registerFindTodoHandler: RegisterFindTodoHandler,
  private val registerFindLastCheckedHandler: RegisterFindLastCheckedHandler,
  private val registerGetHandler: RegisterGetHandler,
  private val registerUpdateHandler: RegisterUpdateHandler,
  private val registerToCheckHandler: RegisterToCheckHandler,
  private val registerCheckedHandler: RegisterCheckedHandler,
  private val findAllSecondaryCategoriesHandler: FindAllSecondaryCategoriesHandler
  private val findHandler: FindHandler
) {
  private val logger = LoggerFactory.getLogger(ModuleConfiguration::class.java)

  init {
    logger.warn("module.rest-context-path.gftaxi-traffic-accident='{}'", contextPath)
    logger.warn("module.version.gftaxi-traffic-accident='{}'", version)
  }

  /** Register a `RouterFunction<ServerResponse>` with all routers for this module */
  @Bean("$MODULE.Routes")
  @ConditionalOnMissingBean(name = ["$MODULE.Routes"])
  fun trafficAccidentRoutes() = router {
    contextPath.nest {
      // GET /category/{sn}/children 获取指定一级分类下的二级分类列表（按一级分类的编码）
      FindSecondaryCategoriesHandler.REQUEST_PREDICATE.invoke(findSecondaryCategoriesHandler::handle)
      // GET /category/group 获取按一级分类编码分组的所有二级分类列表
      FindAllSecondaryCategoriesHandler.REQUEST_PREDICATE.invoke(findAllSecondaryCategoriesHandler::handle)

      //==== 事故报案相关 ====
      // GET /accident-draft 获取事故报案视图信息
      DraftFindHandler.REQUEST_PREDICATE.invoke(draftFindHandler::handle)
      // GET /accident-draft/{id} 获取指定 ID 的报案信息
      DraftGetHandler.REQUEST_PREDICATE.invoke(draftGetHandler::handle)
      // POST /accident-draft 上报案件信息
      DraftSubmitHandler.REQUEST_PREDICATE.invoke(draftSubmitHandler::handle)
      // PUT /accident-draft/{code} 更新事故报案信息
      DraftUpdateHandler.REQUEST_PREDICATE.invoke(draftUpdateHandler::handle)

      //==== 事故登记相关 ====
      // GET /accident-register/stat/summary   获取汇总统计信息
      RegisterStatSummaryHandler.REQUEST_PREDICATE.invoke(registerStatSummaryHandler::handle)
      // GET /accident-register/todo           获取待登记、待审核案件信息
      RegisterFindTodoHandler.REQUEST_PREDICATE.invoke(registerFindTodoHandler::handle)
      // GET /accident-register/last-checked   获取已审核案件的最后审核信息
      RegisterFindLastCheckedHandler.REQUEST_PREDICATE.invoke(registerFindLastCheckedHandler::handle)
      // GET /accident-register/{id}           获取案件信息
      RegisterGetHandler.REQUEST_PREDICATE.invoke(registerGetHandler::handle)
      // PATCH /accident-register/{id}         更新案件信息
      RegisterUpdateHandler.REQUEST_PREDICATE.invoke(registerUpdateHandler::handle)
      // POST /accident-register/to-check/{id} 提交案件信息
      RegisterToCheckHandler.REQUEST_PREDICATE.invoke(registerToCheckHandler::handle)
      // POST /accident-register/checked/{id}  审核案件信息
      RegisterCheckedHandler.REQUEST_PREDICATE.invoke(registerCheckedHandler::handle)

      //==== 事故报告相关 ====
      // GET /accident-report 获取指定状态的案件的分页信息
      FindHandler.REQUEST_PREDICATE.invoke(findHandler::handle)

      //==== 全局 ====
      // GET
      GET("/") { ServerResponse.ok().contentType(TEXT_PLAIN).syncBody("gftaxi-traffic-accident-$version") }
      // OPTIONS /*
      OPTIONS("/**") { ServerResponse.noContent().build() }
    }
  }
}