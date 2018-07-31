package cn.gftaxi.traffic.accident.starter.webflux

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import java.time.OffsetDateTime

/**
 * Application WebFlux Configuration.
 *
 * see [WebFlux config API](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-config-enable)
 *
 * @author RJ
 */
@Configuration
@EnableWebFlux
class WebFluxConfiguration @Autowired constructor(
  @Value("\${app.version.traffic-accident: UNKNOWN}")
  private val version: String
) : WebFluxConfigurer {
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
      .allowedHeaders("Authorization", "Content-Type")
      //.exposedHeaders("header1")
      .allowCredentials(false)
      .maxAge(1800) // seconds
  }

  /**
   * Other application routes.
   */
  @Bean
  fun rootRoutes() = router {
    val now = OffsetDateTime.now()
    "/".nest {
      GET("/", {
        ok().contentType(MediaType.TEXT_HTML)
          .syncBody("<h2>GFTaxi traffic-accident Server</h2><div>Version : $version</div><div>Start at : $now</div>")
      })
    }
  }
}