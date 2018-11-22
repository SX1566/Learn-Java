package cn.gftaxi.traffic.accident.starter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import java.time.OffsetDateTime

/**
 * Application Configuration.
 *
 *
 * @author RJ
 */
@Configuration("cn.gftaxi.traffic.accident.starter.AppConfiguration")
@EnableWebFlux
class AppConfiguration @Autowired constructor(
  @Value("\${module.version.simter:UNKNOWN}") private val simterVersion: String,
  @Value("\${module.version.simter-kv:UNKNOWN}") private val kvVersion: String,
  @Value("\${module.version.simter-category:UNKNOWN}") private val categoryVersion: String,
  @Value("\${module.version.gftaxi-traffic-accident:UNKNOWN}") private val accidentVersion: String
) {
  /**
   * Register by method [DelegatingWebFluxConfiguration.setConfigurers].
   *
   * See [WebFlux config API](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-config-enable)
   */
  @Bean
  fun rootWebFluxConfigurer(): WebFluxConfigurer {
    return object : WebFluxConfigurer {
      /**
       * CORS config.
       *
       * See [Enabling CORS](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-cors)
       */
      override fun addCorsMappings(registry: CorsRegistry?) {
        // Enabling CORS for the whole application
        // By default all origins and GET, HEAD, and POST methods are allowed
        registry!!.addMapping("/**")
          .allowedOrigins("*")
          .allowedMethods("*")
          .allowedHeaders("Authorization", "Content-Type", "Content-Disposition")
          .exposedHeaders("Location")
          .allowCredentials(false)
          .maxAge(1800) // seconds
      }
    }
  }

  private val startTime = OffsetDateTime.now()
  private val rootPage: String = """
    <h2>交通事故微服务</h2>
    <div>系统启动时间：$startTime</div>
    <div>模块版本信息：</div>
    <ul>
      <li>gftaxi-traffic-accident-$accidentVersion</li>
      <li>simter-$simterVersion</li>
      <li>simter-kv-$kvVersion</li>
      <li>simter-category-$categoryVersion</li>
    </ul>
  """.trimIndent()

  /**
   * Other application routes.
   */
  @Bean
  fun rootRoutes() = router {
    "/".nest { GET("/", { ok().contentType(MediaType.TEXT_HTML).syncBody(rootPage) }) }
  }
}