package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故登记 Dao 实现。
 *
 * @author JF
 */
@Component
class AccidentRegisterDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager
) : AccidentRegisterDao {
  @Suppress("UNCHECKED_CAST")
  override fun statSummary(): Flux<AccidentRegisterDto4StatSummary> {
    val now = LocalDateTime.now()
    val lastMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-01 00:00:00"))
    val currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM-01 00:00:00"))
    val nextMonth = now.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-01 00:00:00"))
    val currentYear = now.format(DateTimeFormatter.ofPattern("yyyy-01-01 00:00:00"))
    val nextYear = now.plusYears(1).format(DateTimeFormatter.ofPattern("yyyy-01-01 00:00:00"))
    val sql = """
      select '本月' scope, count(d.code) total,
        count(case when r.status = 8 then 0 else null end) checked,
        count(case when r.status in (2, 4) then 0 else null end) checking,
        count(case when d.status = 1 then 0 else null end) drafting,
        count(case when d.overdue then 0 else null end) overdue_draft,
        count(case when r.overdue_register then 0 else null end) overdue_register
      from gf_accident_draft d
      left join gf_accident_register r on r.code = d.code
      where d.happen_time >= :currentMonth and d.happen_time < :nextMonth

      union all

      select '上月' scope, count(d.code) total,
        count(case when r.status = 8 then 0 else null end) checked,
        count(case when r.status in (2, 4) then 0 else null end) checking,
        count(case when d.status = 1 then 0 else null end) drafting,
        count(case when d.overdue then 0 else null end) overdue_draft,
        count(case when r.overdue_register then 0 else null end) overdue_register
      from gf_accident_draft d
      left join gf_accident_register r on r.code = d.code
      where d.happen_time >= :lastMonth and d.happen_time < :currentMonth

      union all

      select '本年' scope, count(d.code) total,
        count(case when r.status = 8 then 0 else null end) checked,
        count(case when r.status in (2, 4) then 0 else null end) checking,
        count(case when d.status = 1 then 0 else null end) drafting,
        count(case when d.overdue then 0 else null end) overdue_draft,
        count(case when r.overdue_register then 0 else null end) overdue_register
      from gf_accident_draft d
      left join gf_accident_register r on r.code = d.code
      where d.happen_time >= :currentYear and d.happen_time < :nextYear
      """.trimIndent()
    return Flux.fromIterable(
      em.createNativeQuery(sql, AccidentRegisterDto4StatSummary::class.java)
        .setParameter("lastMonth", lastMonth)
        .setParameter("currentMonth", currentMonth)
        .setParameter("nextMonth", nextMonth)
        .setParameter("currentYear", currentYear)
        .setParameter("nextYear", nextYear)
        .resultList as List<AccidentRegisterDto4StatSummary>
    )
  }

  override fun findTodo(status: AccidentRegister.Status?): Flux<AccidentRegisterDto4Todo> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun findChecked(pageNo: Int, pageSize: Int, status: AccidentRegister.Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4Checked>> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}