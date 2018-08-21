package cn.gftaxi.traffic.accident.dao.jdbc

import cn.gftaxi.traffic.accident.Utils.polishCarPlate
import cn.gftaxi.traffic.accident.dao.BcDao
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDate
import javax.sql.DataSource

/**
 * BC 系统相关信息 Dao 的实现。
 *
 * @author RJ
 */
@Component
class BcDaoImpl @Autowired constructor(
  @Qualifier("bcDataSource") private val dataSource: DataSource
) : BcDao {
  private val logger = LoggerFactory.getLogger(BcDaoImpl::class.java)
  private var jdbc: NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

  private fun getMotorcadeNameBlock(carPlate: String, date: LocalDate): String? {
    val sql = """
      select m.name
      from bs_car c, car__find_history_company_and_motorcade(c.id, :date) cm
      inner join bs_motorcade m on m.id = cm.motorcade_id
      where (c.plate_no = :plate or (concat(c.plate_type, c.plate_no) = :plate))
      """.trimIndent()
    return try {
      jdbc.queryForObject(sql, mapOf(
        "plate" to carPlate,
        "date" to java.sql.Date.valueOf(date)
      ), String::class.java)
    } catch (e: EmptyResultDataAccessException) {
      null
    }
  }

  override fun getMotorcadeName(carPlate: String, date: LocalDate): Mono<String> {
    val motorcadeName = getMotorcadeNameBlock(polishCarPlate(carPlate), date)
    return if (motorcadeName == null) Mono.just("") else Mono.just(motorcadeName)
  }

    }
  }
}