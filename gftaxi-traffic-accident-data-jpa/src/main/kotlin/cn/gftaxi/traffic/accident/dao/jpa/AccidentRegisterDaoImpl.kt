package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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

  @Suppress("UNCHECKED_CAST")
  override fun findTodo(status: AccidentRegister.Status?): Flux<AccidentRegisterDto4Todo> {
    val where = when (status) {
      AccidentRegister.Status.Draft -> "where d.status = :accidentDraftTodo"
      AccidentRegister.Status.ToCheck -> "where d.status = :accidentDraftDone and r.status = :accidentRegisterToCheck"
      null -> "where d.status = :accidentDraftTodo or (d.status = :accidentDraftDone and r.status = :accidentRegisterToCheck)"
      else -> throw IllegalArgumentException("指定的状态条件 $status 不在允许的范围内！")
    }
    val sql = """
      select d.code, d.car_plate, d.driver_name,
      not exists(select 0 from bs_carman c where c.name = d.driver_name) outside_driver, d.happen_time, d.hit_form,
      d.hit_type, d.location, d.author_name, d.author_id, d.report_time, d.overdue overdue_report, r.register_time,
      r.overdue_register,
      (
        select operate_time
        from gf_accident_operation o
        where o.target_type = :accidentRegister
          and o.target_id = r.id
          and o.operation_type = :confirmation
        order by operate_time desc
        limit 1
      ) submit_time
      from gf_accident_draft d
        left join gf_accident_register r on r.code = d.code
      $where
      order by d.code
      """.trimIndent()

    val query = em.createNativeQuery(sql, AccidentRegisterDto4Todo::class.java)
      .setParameter("accidentRegister", AccidentOperation.TargetType.Register.value())
      .setParameter("confirmation", AccidentOperation.OperationType.Confirmation.value())
    when (status) {
      AccidentRegister.Status.Draft -> query.setParameter("accidentDraftTodo", AccidentDraft.Status.Todo.value())
      AccidentRegister.Status.ToCheck -> {
        query.setParameter("accidentDraftDone", AccidentDraft.Status.Done.value())
          .setParameter("accidentRegisterToCheck", AccidentRegister.Status.ToCheck.value())
      }
      null -> {
        query.setParameter("accidentDraftTodo", AccidentDraft.Status.Todo.value())
          .setParameter("accidentDraftDone", AccidentDraft.Status.Done.value())
          .setParameter("accidentRegisterToCheck", AccidentRegister.Status.ToCheck.value())
      }
      else -> throw IllegalArgumentException("指定的状态条件 $status 不在允许的范围内！")
    }
    return Flux.fromIterable(query.resultList as List<AccidentRegisterDto4Todo>)
  }

  @Suppress("UNCHECKED_CAST")
  override fun findChecked(pageNo: Int, pageSize: Int, status: AccidentRegister.Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4Checked>> {
    if (null != status && status != AccidentRegister.Status.Rejected && status != AccidentRegister.Status.Approved) {
      throw IllegalArgumentException("指定的状态条件 $status 不在允许的范围内！")
    }
    val hasSearch = null != search
    val where =
      "where r.status in (:status) ${if (hasSearch) "and (r.code like :search or r.car_plate like :search)" else ""}"
    val rowsSql = """
      with last_operation(target_id, checked_time, checked_count) as (
        select target_id, max(operate_time), count(operate_time)
        from gf_accident_operation
        where target_type = :targetType and operation_type in (:operationType)
        group by target_type, target_id
      )
      select distinct r.code, r.car_plate, r.party_driver_name driver_name,
        not exists(select 0 from bs_carman c where c.name = r.party_driver_name) outside_driver,
        r.status checked_result, o.comment checked_comment, o.operator_name checker_name, l.checked_count,
        l.checked_time, o.attachment_name, o.attachment_id
      from gf_accident_register r
        inner join last_operation l on l.target_id = r.id
        inner join gf_accident_operation o on o.target_type = :targetType and o.operation_type in (:operationType)
          and o.target_id = r.id and o.operate_time = l.checked_time
      $where
      order by r.code desc
      """.trimIndent()
    val countSql = "select count(r.code) from gf_accident_register r $where"

    val rowsQuery = em.createNativeQuery(rowsSql, AccidentRegisterDto4Checked::class.java)
      .setParameter("targetType", AccidentOperation.TargetType.Register.value())
      .setParameter("operationType",
        listOf(AccidentOperation.OperationType.Approval.value(), AccidentOperation.OperationType.Rejection.value())
      )
      .setParameter("status",
        status?.value() ?: listOf(AccidentRegister.Status.Approved.value(), AccidentRegister.Status.Rejected.value())
      )
      .setFirstResult(tech.simter.data.Page.calculateOffset(pageNo, pageSize))
      .setMaxResults(tech.simter.data.Page.toValidCapacity(pageSize))
    val countQuery = em.createNativeQuery(countSql)
      .setParameter("status",
        status?.value() ?: listOf(AccidentRegister.Status.Approved.value(), AccidentRegister.Status.Rejected.value())
      )
    if (hasSearch) {
      val fuzzySearch = if (!search!!.contains("%")) "%$search%" else search
      rowsQuery.setParameter("search", fuzzySearch)
      countQuery.setParameter("search", fuzzySearch)
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