package cn.gftaxi.traffic.accident.rest.webflux

import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindAllSecondaryCategoriesHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindSecondaryCategoriesHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

private const val MODULE = "cn.gftaxi.traffic.accident.rest.webflux"

/**
 * All configuration for this module.
 *
 * Register a `RouterFunction<ServerResponse>` with all routers for this module.
 * The default context-path of this router is '/'. And can be config by property `gftaxi.rest.context-path.traffic-accident`.
 *
 * @author JF
 */
@Configuration("$MODULE.ModuleConfiguration")
@ComponentScan(MODULE)
@EnableWebFlux
class ModuleConfiguration @Autowired constructor(
  @Value("\${gftaxi.rest.context-path.traffic-accident:/}") private val contextPath: String,
  private val findSecondaryCategoriesHandler: FindSecondaryCategoriesHandler,
  @Value("\${app.version.traffic-accident:NOT_SET}") private val version: String,
  private val accidentDraftHandler: AccidentDraftHandler,
  private val accidentRegisterStatSummaryHandler: StatSummaryHandler,
  private val accidentRegisterFindTodoHandler: FindTodoHandler,
  private val accidentRegisterFindCheckedHandler: FindCheckedHandler,
  private val accidentRegisterUpdateHandler: UpdateHandler,
  private val accidentRegisterToCheckHandler: ToCheckHandler,
  private val accidentRegisterCheckedHandler: CheckedHandler,
  private val findAllSecondaryCategoriesHandler: FindAllSecondaryCategoriesHandler
) {
  private val logger = LoggerFactory.getLogger(ModuleConfiguration::class.java)

  init {
    logger.warn("gftaxi.rest.context-path.traffic-accident='{}'", contextPath)
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
      // GET /accident-draft 获取事故报案独立视图的分页数据
      AccidentDraftHandler.FIND_REQUEST_PREDICATE.invoke(accidentDraftHandler::find)
      // GET /accident-draft/{code} 获取指定编号的报案信息
      AccidentDraftHandler.GET_REQUEST_PREDICATE.invoke(accidentDraftHandler::get)
      // POST /accident-draft 上报案件信息
      AccidentDraftHandler.SUBMIT_REQUEST_PREDICATE.invoke(accidentDraftHandler::submit)
      // PUT /accident-draft/{code} 更新事故报案信息
      AccidentDraftHandler.UPDATE_REQUEST_PREDICATE.invoke(accidentDraftHandler::update)

      //==== 事故登记相关 ====
      // GET /accident-register/stat/summary 获取汇总统计信息
      StatSummaryHandler.REQUEST_PREDICATE.invoke(accidentRegisterStatSummaryHandler::handle)
      // GET /accident-register/todo         获取待登记、待审核案件信息
      FindTodoHandler.REQUEST_PREDICATE.invoke(accidentRegisterFindTodoHandler::handle)
      // GET /accident-register/checked      获取已审核案件信息
      FindCheckedHandler.REQUEST_PREDICATE.invoke(accidentRegisterFindCheckedHandler::handle)
      // PATCH /accident-register/{id}       更新案件信息
      UpdateHandler.REQUEST_PREDICATE.invoke(accidentRegisterUpdateHandler::handle)
      // POST /accident-register/to-check/{id} 提交案件信息
      ToCheckHandler.REQUEST_PREDICATE.invoke(accidentRegisterToCheckHandler::handle)
      // POST /accident-register/checked/{id} 审核案件信息
      CheckedHandler.REQUEST_PREDICATE.invoke(accidentRegisterCheckedHandler::handle)

      //==== 全局 ====
      // GET
      GET("/", { ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).syncBody("gftaxi-traffic-accident module v$version") })
      // OPTIONS /*
      OPTIONS("/**", { ServerResponse.noContent().build() })
    }
  }
}