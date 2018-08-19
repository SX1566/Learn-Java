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

  override fun getMotorcadeName(carPlate: String, date: LocalDate): Mono<String> {
    val sql = """
      select (case when hm.id is null then m.name else hm.name end) as name
      from bs_car c
      left join bs_motorcade m on m.id = c.motorcade_id
      left join bs_car_driver_history h on h.to_car_id = c.id and h.move_type = 6
      left join bs_motorcade hm on hm.id = h.to_motorcade_id
      where (c.plate_no = :plate or (concat(c.plate_type, c.plate_no) = :plate))
      -- 寻找最接近 date 参数的最早的那条迁移记录
      order by (case sign(:date - h.move_date) when 0 then 2 when 1 then 1 else -1 end) desc
        , abs(:date - h.move_date) asc, h.move_date asc
      limit 1
      """.trimIndent()
    logger.debug("sql={}", sql)
    return try {
      val motorcadeName = jdbc.queryForObject(sql, mapOf(
        "plate" to polishCarPlate(carPlate),
        "date" to java.sql.Date.valueOf(date)
      ), String::class.java)
      if (motorcadeName == null) Mono.just("") else Mono.just(motorcadeName)
    } catch (e: EmptyResultDataAccessException) {
      Mono.just("")
    }
  }
}