package cn.gftaxi.traffic.accident.rest.webflux

import cn.gftaxi.traffic.accident.rest.webflux.handler.AccidentDraftHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindSecondaryCategoriesHandler
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
  private val accidentDraftHandler: AccidentDraftHandler
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

      // GET /accident-draft 获取事故报案独立视图的分页数据
      AccidentDraftHandler.FIND_REQUEST_PREDICATE.invoke(accidentDraftHandler::find)
      // GET /accident-draft/{code} 获取指定编号的报案信息
      AccidentDraftHandler.GET_REQUEST_PREDICATE.invoke(accidentDraftHandler::get)
      // POST /accident-draft 上报案件信息
      AccidentDraftHandler.SUBMIT_REQUEST_PREDICATE.invoke(accidentDraftHandler::submit)
      // PUT /accident-draft/{code} 更新事故报案信息
      AccidentDraftHandler.UPDATE_REQUEST_PREDICATE.invoke(accidentDraftHandler::update)

      // GET
      GET("/", { ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).syncBody("gftaxi-traffic-accident module v$version") })
      // OPTIONS /*
      OPTIONS("/**", { ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).syncBody("options") })
    }
  }
}