package cn.gftaxi.traffic.accident.rest.webflux

import cn.gftaxi.traffic.accident.rest.webflux.handler.FindMotorcadeHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindAllSecondaryCategoriesHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.FindSecondaryCategoriesHandler
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
import tech.simter.operation.rest.webflux.handler.operation.FindByClusterHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.FindHandler as DraftFindHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.GetHandler as DraftGetHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.SubmitHandler as DraftSubmitHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.draft.UpdateHandler as DraftUpdateHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.CheckedHandler as RegisterCheckedHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.FindHandler as RegisterFindHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.GetHandler as RegisterGetHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatMonthlySummaryHandler as RegisterStatMonthlySummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatQuarterlySummaryHandler as RegisterStatQuarterlySummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.StatYearlySummaryHandler as RegisterStatYearlySummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.ToCheckHandler as RegisterToCheckHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.register.UpdateHandler as RegisterUpdateHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.CheckedHandler as ReportCheckedHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.FindHandler as ReportFindHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.GetHandler as ReportGetHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.StatMonthlySummaryHandler as ReportStatMonthlySummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.StatQuarterlySummaryHandler as ReportStatQuarterlySummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.StatYearlySummaryHandler as ReportStatYearlySummaryHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.ToCheckHandler as ReportToCheckHandler
import cn.gftaxi.traffic.accident.rest.webflux.handler.report.UpdateHandler as ReportUpdateHandler

private const val MODULE = "cn.gftaxi.traffic.accident.rest.webflux"

/**
 * All configuration for this module.
 *
 * Register a `RouterFunction<ServerResponse>` with all routers for this module.
 * The default context-path of this router is '/'. And can be config by property `gftaxi.rest.context-path.traffic-accident`.
 *
 * @author JF
 * @author RJ
 *
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
  private val registerStatMonthlySummaryHandler: RegisterStatMonthlySummaryHandler,
  private val registerStatQuarterlySummaryHandler: RegisterStatQuarterlySummaryHandler,
  private val registerStatYearlySummaryHandler: RegisterStatYearlySummaryHandler,
  private val registerFindHandler: RegisterFindHandler,
  private val registerGetHandler: RegisterGetHandler,
  private val registerUpdateHandler: RegisterUpdateHandler,
  private val registerToCheckHandler: RegisterToCheckHandler,
  private val registerCheckedHandler: RegisterCheckedHandler,
  private val reportFindHandler: ReportFindHandler,
  private val findAllSecondaryCategoriesHandler: FindAllSecondaryCategoriesHandler,
  private val reportGetHandler: ReportGetHandler,
  private val reportUpdateHandler: ReportUpdateHandler,
  private val reportToCheckHandler: ReportToCheckHandler,
  private val reportCheckedHandler: ReportCheckedHandler,
  private val reportStatMonthlySummaryHandler: ReportStatMonthlySummaryHandler,
  private val reportStatQuarterlySummaryHandler: ReportStatQuarterlySummaryHandler,
  private val reportStatYearlySummaryHandler: ReportStatYearlySummaryHandler,
  private val findByClusterHandler: FindByClusterHandler,
  private val findMotorcadeHandler: FindMotorcadeHandler
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
      // GET /motorcade 获取所属车队信息列表
      FindMotorcadeHandler.REQUEST_PREDICATE.invoke(findMotorcadeHandler::handle)

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
      // GET /accident-register/stat-monthly-summary   月度汇总统计信息
      RegisterStatMonthlySummaryHandler.REQUEST_PREDICATE.invoke(registerStatMonthlySummaryHandler::handle)
      // GET /accident-register/stat-quarterly-summary 季度汇总统计信息
      RegisterStatQuarterlySummaryHandler.REQUEST_PREDICATE.invoke(registerStatQuarterlySummaryHandler::handle)
      // GET /accident-register/stat-yearly-summary    年度汇总统计信息
      RegisterStatYearlySummaryHandler.REQUEST_PREDICATE.invoke(registerStatYearlySummaryHandler::handle)
      // GET /accident-register                获取视图信息
      RegisterFindHandler.REQUEST_PREDICATE.invoke(registerFindHandler::handle)
      // GET /accident-register/{id}           获取案件信息
      RegisterGetHandler.REQUEST_PREDICATE.invoke(registerGetHandler::handle)
      // PATCH /accident-register/{id}         更新案件信息
      RegisterUpdateHandler.REQUEST_PREDICATE.invoke(registerUpdateHandler::handle)
      // POST /accident-register/to-check/{id} 提交案件信息
      RegisterToCheckHandler.REQUEST_PREDICATE.invoke(registerToCheckHandler::handle)
      // POST /accident-register/checked/{id}  审核案件信息
      RegisterCheckedHandler.REQUEST_PREDICATE.invoke(registerCheckedHandler::handle)

      //==== 事故报告相关 ====
      // GET /accident-report                 获取指定状态案件的分页信息
      ReportFindHandler.REQUEST_PREDICATE.invoke(reportFindHandler::handle)
      // GET /accident-report/{id}            获取指定ID的事故报告信息
      ReportGetHandler.REQUEST_PREDICATE.invoke(reportGetHandler::handle)
      // PATCH /accident-report/{id}          更新事故报告信息
      ReportUpdateHandler.REQUEST_PREDICATE.invoke(reportUpdateHandler::handle)
      // POST /accident-report/to-check/{id}  提交事故报告信息
      ReportToCheckHandler.REQUEST_PREDICATE.invoke(reportToCheckHandler::handle)
      // PATCH /accident-report/{id}          审核事故报告信息
      ReportCheckedHandler.REQUEST_PREDICATE.invoke(reportCheckedHandler::handle)
      // GET /accident-report/stat-monthly-summary    月度汇总统计信息
      ReportStatMonthlySummaryHandler.REQUEST_PREDICATE.invoke(reportStatMonthlySummaryHandler::handle)
      // GET /accident-report/stat-quarterly-summary  季度汇总统计信息
      ReportStatQuarterlySummaryHandler.REQUEST_PREDICATE.invoke(reportStatQuarterlySummaryHandler::handle)
      // GET /accident-report/stat-yearly-summary     年度汇总统计信息
      ReportStatYearlySummaryHandler.REQUEST_PREDICATE.invoke(reportStatYearlySummaryHandler::handle)

      // GET /cluster/{cluster} find Operations by cluster
      FindByClusterHandler.REQUEST_PREDICATE.invoke(findByClusterHandler::handle)

      //==== 全局 ====
      // GET
      GET("/") { ServerResponse.ok().contentType(TEXT_PLAIN).syncBody("gftaxi-traffic-accident-$version") }
      // OPTIONS /*
      OPTIONS("/**") { ServerResponse.noContent().build() }
    }
  }
}