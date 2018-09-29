package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.data.Page.calculateOffset
import tech.simter.operation.OperationType
import java.time.Year
import java.time.YearMonth
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故报告 [AccidentReportDao] 实现。
 *
 * @author zh
 */
@Component
@Transactional
class AccidentReportDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager
) : AccidentReportDao {
  @Suppress("UNCHECKED_CAST")
  override fun find(pageNo: Int, pageSize: Int, statuses: List<AccidentReport.Status>?, search: String?)
    : Mono<Page<AccidentReportDto4View>> {
    // 生成sql语句
    val searchQl = search?.let {
      """
        and ( draft.code like :search
          or register.motorcade_name like :search
          or register.car_plate like :search
          or register.driver_dame like :search )
      """.trimIndent()
    } ?: ""
    val statusesQl = statuses?.let {
      if (it.isEmpty()) {
        ""
      } else {
        var postfix = ")"
        it.joinToString(
          separator = ", ",
          prefix = "and ( report.status in (",
          postfix = ")",
          transform = {
            if (it == AccidentReport.Status.Draft) {
              postfix = " or report.status is null )"
            }
            "${it.value()}"
          }
        ) + postfix
      }
    } ?: ""
    val constant = mapOf(
      "peopleType" to "自车",
      "operationTypeApproval" to OperationType.Approval.toString(),
      "operationTypeRejection" to OperationType.Rejection.toString(),
      "targetType" to AccidentReport::class.simpleName
    )
    val infoWhereQl = "where register.status = ${AccidentRegister.Status.Approved.value()} $searchQl $statusesQl"
    val operationWhereQl = """
      where (type = :operationTypeRejection or type = :operationTypeApproval)
        and target_type = :targetType
    """.trimIndent()
    val limitQl = "limit $pageSize offset ${calculateOffset(pageNo, pageSize)}"
    val countQl = """
        select count(0)
        from gf_accident_register as register
        inner join gf_accident_draft as draft on register.id = draft.id
        left join gf_accident_report as report on report.id = register.id
        $infoWhereQl
      """.trimIndent()
    val rowQl = """
      with
      -- info表，查出请求分页中的事故本身信息的数据
      info(
        id, code, status, motorcade_name, car_plate, car_model,
        driver_name, driver_type, happen_time, location, level, hit_form,
        draft_time, overdue_draft, register_time, overdue_register,
        report_time, overdue_report, appoint_driver_return_time
      ) as (
        select register.id, draft.code, report.status,
          register.motorcade_name, register.car_plate,
          register.car_model, register.driver_name,
          register.driver_type, register.happen_time,
          draft.location, register.level, register.hit_form,
          draft.draft_time, draft.overdue_draft,
          register.register_time, register.overdue_register,
          report.report_time, report.overdue_report,
          report.appoint_driver_return_time
        from gf_accident_register as register
        inner join gf_accident_draft as draft on draft.id = register.id
        left join gf_accident_report as report on report.id = register.id
        $infoWhereQl
        $limitQl
      ),
      -- checked表，查出每个事故对应的审核次数
      checked(id, checked_count) as (
        select cast(target_id as Int), count(target_id)
        from st_operation
        $operationWhereQl
        group by target_id
      ),
      -- attachment表，查出每个事故最后一次审核的审核信息
      attachment(id, attachments, comment) as (
        select cast(operation.target_id as Int), operation.attachments , operation.comment
        from st_operation as operation
        inner join (
          select max(time) as max_time, target_id
          from st_operation
          $operationWhereQl
          group by target_id
        ) as time on time.target_id = operation.target_id and time.max_time = operation.time
        $operationWhereQl
      )
      select info.id as id, info.code as code, info.status as status,
        info.motorcade_name as motorcade_name, info.car_plate as car_plate,
        info.car_model as car_model, info.driver_name as driver_name,
        info.driver_type as driver_type, info.happen_time as happen_time,
        info.location as location, info.level as level,
        info.hit_form as hit_form, people.duty as duty,
        info.draft_time as draft_time, info.overdue_draft as overdue_draft,
        info.register_time as register_time, info.overdue_register as overdue_register,
        info.report_time as report_time, info.overdue_report as overdue_report,
        info.appoint_driver_return_time as appoint_driver_return_time,
        coalesce(checked.checked_count,0) as checked_count,
        attachment.attachments as attachments,
        attachment.comment as checked_comment
      from info
      left join gf_accident_people as people on info.id = people.pid and people.type = :peopleType
      left join checked on info.id = checked.id
      left join attachment on info.id = attachment.id
      order by info.happen_time desc
    """.trimIndent()

    // 生成Query
    val rowsQuery = em.createNativeQuery(rowQl, AccidentReportDto4View::class.java)
    val countQuery = em.createNativeQuery(countQl)

    // 设置参数
    constant.forEach { key, value ->
      rowsQuery.setParameter(key, value)
    }
    search?.let {
      val searchStr = if (it.contains("%")) it else "%$it%"
      rowsQuery.setParameter("search", searchStr)
      countQuery.setParameter("search", searchStr)
    }
    // 将查询结果返回
    return Mono.just(
      PageImpl(
        rowsQuery.resultList as List<AccidentReportDto4View>,
        PageRequest.of(pageNo - 1, pageSize),
        (countQuery.singleResult as Number).toLong()
      )
    )
  }

  override fun get(id: Int): Mono<AccidentReportDto4Form> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun createBy(accidentDraft: AccidentDraft): Mono<AccidentReport> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getStatus(id: Int): Mono<AccidentReport.Status> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun toCheck(id: Int): Mono<Void> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun checked(id: Int, passed: Boolean): Mono<Boolean> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Boolean> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun statSummaryByMonthly(from: YearMonth, to: YearMonth): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun statSummaryByYearly(from: Year, to: Year): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun statSummaryByQuarterly(from: Year, to: Year): Flux<AccidentReportDto4StatSummary> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}