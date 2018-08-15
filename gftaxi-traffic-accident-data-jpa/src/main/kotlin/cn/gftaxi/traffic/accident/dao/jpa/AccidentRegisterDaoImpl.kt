package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4LastChecked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.dto.ScopeType
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
  fun buildStatSummaryRowSqlByHappenTimeRange(scope: String, from: Int, to: Int): String {
    return """
      select '$scope' scope, count(d.id) total,
        count(case when r.status = ${Approved.value()} then 0 else null end) checked,
        count(case when r.status in (${ToCheck.value()}, ${Rejected.value()}) then 0 else null end) checking,
        count(case when d.status = ${Todo.value()} then 0 else null end) drafting,
        count(case when d.overdue then 0 else null end) overdue_draft,
        count(case when r.overdue then 0 else null end) overdue_register
      from gf_accident_draft d
      left join gf_accident_register r on r.id = d.id
      where cast(to_char(d.happen_time, :format) as int) >= $from
        and cast(to_char(d.happen_time, :format) as int) < $to
      """.trimIndent()
  }

  /**
   * *验证统计范围条件是否正确
   *
   * @param scopeType 统计范围类型，按月、按年和按季度
   * @param from      统计范围开始点
   * @param to        统计范围结束点
   */
  private fun validateScope(scopeType: ScopeType, from: Int?, to: Int?) {
    if (null !== from && null !== to) {
      if (from > to) throw IllegalArgumentException("统计范围的开始时间不可以小于结束时间！")
      if (scopeType !== ScopeType.Monthly && to.minus(from) > 1)
        throw IllegalArgumentException("统计范围不可以大于两年！")
      if (scopeType === ScopeType.Monthly) {
        val scopeGap = Period.between(
          LocalDate.parse("${from}01", DateTimeFormatter.ofPattern("yyyyMMdd")),
          LocalDate.parse("${to}01", DateTimeFormatter.ofPattern("yyyyMMdd"))
        )
        if (scopeGap.years > 2 || (scopeGap.years == 2 && scopeGap.months > 0))
          throw IllegalArgumentException("统计范围不可以大于两年！")
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun statSummary(scopeType: ScopeType, from: Int?, to: Int?): Flux<AccidentRegisterDto4StatSummary> {
    validateScope(scopeType, from, to) // 验证统计范围条件值
    var scopeStart = when {
      null === from -> YearMonth.now()
      else -> when (scopeType) {
        ScopeType.Monthly -> YearMonth.parse(from.toString(), DateTimeFormatter.ofPattern("yyyyMM"))
        ScopeType.Yearly -> YearMonth.parse("${from}01", DateTimeFormatter.ofPattern("yyyyMM"))
        ScopeType.Quarterly -> YearMonth.parse("${from}01", DateTimeFormatter.ofPattern("yyyyMM"))
      }
    }
    val scopeEnd = when {
      null === to -> YearMonth.now()
      else -> when (scopeType) {
        ScopeType.Monthly -> YearMonth.parse(to.toString(), DateTimeFormatter.ofPattern("yyyyMM"))
        ScopeType.Yearly -> YearMonth.parse("${to}01", DateTimeFormatter.ofPattern("yyyyMM"))
        ScopeType.Quarterly -> YearMonth.parse("${to}12", DateTimeFormatter.ofPattern("yyyyMM"))
      }
    }

    var sql = ""
    val dateFormat = when (scopeType) {
      ScopeType.Yearly -> "yyyy"
      else -> "yyyyMM"
    }
    var isFirst = true
    while (!scopeStart.isAfter(scopeEnd)) {
      val scope = when (scopeType) {
        ScopeType.Monthly -> scopeStart.format(DateTimeFormatter.ofPattern("yyyy年MM月"))
        ScopeType.Yearly -> scopeStart.format(DateTimeFormatter.ofPattern("yyyy年"))
        ScopeType.Quarterly -> "${scopeStart.year}年第${Math.ceil(scopeStart.month.value / 3.0).toInt()}季度"
      }
      val tempScopeEnd = when (scopeType) {
        ScopeType.Monthly -> scopeStart.plusMonths(1)
        ScopeType.Yearly -> scopeStart.plusYears(1)
        ScopeType.Quarterly -> scopeStart.plusMonths(3)
      }
      // 迭代生成事故登记汇总统计查询SQL
      sql += (if (isFirst) "" else "\nunion all\n") + buildStatSummaryRowSqlByHappenTimeRange(
        scope, scopeStart.format(DateTimeFormatter.ofPattern(dateFormat)).toInt(),
        tempScopeEnd.format(DateTimeFormatter.ofPattern(dateFormat)).toInt()
      )
      // 更新统计范围开始点
      scopeStart = tempScopeEnd
      isFirst = false
    }
    sql = """
      with stat_summary(scope, total, checked, checking, drafting, overdue_draft, overdue_register) as ($sql)
      select * from stat_summary order by scope desc
    """
    return Flux.fromIterable(
      em.createNativeQuery(sql, AccidentRegisterDto4StatSummary::class.java)
        .setParameter("format", dateFormat)
        .resultList as List<AccidentRegisterDto4StatSummary>
    )
  }

  @Suppress("UNCHECKED_CAST")
  override fun findTodo(status: Status?): Flux<AccidentRegisterDto4Todo> {
    val where = when (status) {
      Draft -> "where d.status = ${Todo.value()}"
      ToCheck -> "where r.status = ${ToCheck.value()}"
      null -> "where d.status = ${Todo.value()} or r.status = ${ToCheck.value()}"
      else -> throw IllegalArgumentException("指定的状态条件 $status 不在允许的范围内！")
    }
    val sql = """
      select d.id, d.code, d.car_plate, d.driver_name, d.happen_time, d.hit_form, d.hit_type,
      (case when r.id is null then null else r.driver_type end) driver_type,
      (case when r.id is null then d.location else r.location_other end) as location,
      r.motorcade_name, d.author_name, d.author_id, d.report_time, d.overdue overdue_report,
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
  override fun findLastChecked(pageNo: Int, pageSize: Int, status: Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4LastChecked>> {
    if (null != status && status != Status.Rejected && status != Status.Approved) {
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
      select distinct r.id, d.code, r.car_plate, r.driver_name, r.driver_type,
        (case when r.id is null then d.location else r.location_other end) as location,
        r.motorcade_name, r.happen_time, o.comment checked_comment, o.operator_name checker_name,
        l.checked_count, l.checked_time, o.attachment_id
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
      ?: listOf(Status.Approved.value(), Status.Rejected.value())
    val rowsQuery = em.createNativeQuery(rowsSql, AccidentRegisterDto4LastChecked::class.java)
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
        rowsQuery.resultList as List<AccidentRegisterDto4LastChecked>,
        PageRequest.of(pageNo - 1, pageSize),
        (countQuery.singleResult as Number).toLong()
      )
    )
  }

  override fun get(id: Int): Mono<AccidentRegister> {
    TODO("not implemented")
  }

  override fun createBy(accidentDraft: AccidentDraft): Mono<AccidentRegister> {
    TODO("not implemented")
  }

  override fun getStatus(id: Int): Mono<Status> {
    TODO("not implemented")
  }
}