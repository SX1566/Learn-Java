package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故登记 Dao 实现。
 *
 * @author JF
 * @author RJ
 */
@Component
class AccidentRegisterDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager
) : AccidentRegisterDao {
  private val logger = LoggerFactory.getLogger(AccidentRegisterDaoImpl::class.java)
  fun buildStatSummaryRowSqlByHappenTimeRange(scope: String, geKey: String, ltKey: String): String {
    return """
      select '$scope' scope, count(d.id) total,
        count(case when r.status = ${Approved.value()} then 0 else null end) checked,
        count(case when r.status in (${ToCheck.value()}, ${Rejected.value()}) then 0 else null end) checking,
        count(case when d.status = ${Todo.value()} then 0 else null end) drafting,
        count(case when d.overdue then 0 else null end) overdue_draft,
        count(case when r.overdue then 0 else null end) overdue_register
      from gf_accident_draft d
      left join gf_accident_register r on r.id = d.id
      where d.happen_time >= :$geKey and d.happen_time < :$ltKey
      """.trimIndent()
  }

  @Suppress("UNCHECKED_CAST")
  override fun statSummary(): Flux<AccidentRegisterDto4StatSummary> {
    val now = OffsetDateTime.now().truncatedTo(DAYS)     // zero times
    val currentMonth = now.withDayOfMonth(1)             // yyyy-MM-01 00:00:00
    val lastMonth = currentMonth.minusMonths(1)          // yyyy-MM-01 00:00:00
    val nextMonth = currentMonth.plusMonths(1)           // yyyy-MM-01 00:00:00
    val currentYear = currentMonth.withMonth(1)          // yyyy-01-01 00:00:00
    val nextYear = currentYear.plusYears(1)              // yyyy-01-01 00:00:00
    val sql = buildStatSummaryRowSqlByHappenTimeRange("本月", "currentMonth", "nextMonth") +
      "\nunion all\n" +
      buildStatSummaryRowSqlByHappenTimeRange("上月", "lastMonth", "currentMonth") +
      "\nunion all\n" +
      buildStatSummaryRowSqlByHappenTimeRange("本年", "currentYear", "nextYear")
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

  @Suppress("UNCHECKED_CAST")
  override fun findTodo(status: AccidentRegister.Status?): Flux<AccidentRegisterDto4Todo> {
    val where = when (status) {
      Draft -> "where d.status = ${Todo.value()}"
      ToCheck -> "where r.status = ${ToCheck.value()}"
      null -> "where d.status = ${Todo.value()} or r.status = ${ToCheck.value()}"
      else -> throw IllegalArgumentException("指定的状态条件 $status 不在允许的范围内！")
    }
    val sql = """
      select d.id, d.code, d.car_plate, d.driver_name, d.happen_time, d.hit_form, d.hit_type,
      (case when r.id is null then null else r.driver_type end) driver_type,
      (case when r.id is null then d.location else r.location_other end) location,
      d.author_name, d.author_id, d.report_time, d.overdue overdue_report,
      r.register_time, r.overdue overdue_register,
      (case when d.status = ${Todo.value()} then null else (
        select operate_time
        from gf_accident_operation o
        where o.target_type = ${TargetType.Register.value()}
          and o.target_id = r.id
          and o.operation_type = ${OperationType.Confirmation.value()}
        order by operate_time desc
        limit 1
      ) end) as submit_time
      from gf_accident_draft d
      left join gf_accident_register r on r.id = d.id
      $where
      order by d.happen_time desc
      """.trimIndent()

    val query = em.createNativeQuery(sql, AccidentRegisterDto4Todo::class.java)
    val list = query.resultList as List<AccidentRegisterDto4Todo>
    if (logger.isDebugEnabled) logger.debug("findTodo=$list")
    return Flux.fromIterable(list)
  }

  @Suppress("UNCHECKED_CAST")
  override fun findChecked(pageNo: Int, pageSize: Int, status: AccidentRegister.Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4Checked>> {
    if (null != status && status != AccidentRegister.Status.Rejected && status != AccidentRegister.Status.Approved) {
      throw IllegalArgumentException("指定的状态条件 $status 不在允许的范围内！")
    }
    val hasSearch = null != search
    val where =
      "where r.status in (:status) ${if (hasSearch) "and (d.code like :search or r.car_plate like :search)" else ""}"
    val rowsSql = """
      with last_operation(target_id, checked_time, checked_count) as (
        select target_id, max(operate_time), count(operate_time)
        from gf_accident_operation
        where target_type = ${TargetType.Register.value()} and operation_type in (:operationType)
        group by target_type, target_id
      )
      select distinct r.id, d.code, r.car_plate, r.driver_name, r.driver_type, r.happen_time,
        r.status checked_result, o.comment checked_comment, o.operator_name checker_name, l.checked_count,
        l.checked_time, o.attachment_name, o.attachment_id
      from gf_accident_register r
      inner join gf_accident_draft d on d.id = r.id
      inner join last_operation l on l.target_id = r.id
      inner join gf_accident_operation o on o.target_type = ${TargetType.Register.value()}
        and o.operation_type in (:operationType)
        and o.target_id = r.id and o.operate_time = l.checked_time
      $where
      order by r.happen_time desc
      """.trimIndent()
    val countSql = "select count(r.id) from gf_accident_register r $where"

    val statusValue = status?.value()
      ?: listOf(AccidentRegister.Status.Approved.value(), AccidentRegister.Status.Rejected.value())
    val rowsQuery = em.createNativeQuery(rowsSql, AccidentRegisterDto4Checked::class.java)
      .setParameter("operationType", listOf(OperationType.Approval.value(), OperationType.Rejection.value()))
      .setParameter("status", statusValue)
      .setFirstResult(tech.simter.data.Page.calculateOffset(pageNo, pageSize))
      .setMaxResults(tech.simter.data.Page.toValidCapacity(pageSize))
    val countQuery = em.createNativeQuery(countSql)
      .setParameter("status", statusValue)
    if (hasSearch) {
      val searchValue = if (!search!!.contains("%")) "%$search%" else search
      rowsQuery.setParameter("search", searchValue)
      countQuery.setParameter("search", searchValue)
    }

    return Mono.just(
      PageImpl(
        rowsQuery.resultList as List<AccidentRegisterDto4Checked>,
        PageRequest.of(pageNo - 1, pageSize),
        (countQuery.singleResult as Number).toLong()
      )
    )
  }
}