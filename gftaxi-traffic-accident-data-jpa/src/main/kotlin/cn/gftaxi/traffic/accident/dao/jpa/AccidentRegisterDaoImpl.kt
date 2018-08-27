package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.Utils.calculateYears
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentRegisterJpaRepository
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.AccidentCar
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentPeople
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.isOverdue
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.PersistenceContext
import javax.persistence.Query

/**
 * 事故登记 Dao 实现。
 *
 * @author JF
 * @author RJ
 */
@Component
@Transactional
class AccidentRegisterDaoImpl @Autowired constructor(
  @Value("\${app.register-overdue-hours:24}") private val overdueHours: Long,
  @PersistenceContext private val em: EntityManager,
  private val repository: AccidentRegisterJpaRepository,
  private val bcDao: BcDao
) : AccidentRegisterDao {
  private val logger = LoggerFactory.getLogger(AccidentRegisterDaoImpl::class.java)
  private val overdueSeconds = overdueHours * 60 * 60

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
      (case when r.id is null then d.location else r.location end) as location,
      (case when r.id is null then d.motorcade_name else r.motorcade_name end) as motorcade_name,
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
  override fun findLastChecked(pageNo: Int, pageSize: Int, status: Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4LastChecked>> {
    if (null != status && status != Rejected && status != Approved) {
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
        (case when r.id is null then d.location else r.location end) as location,
        (case when r.id is null then d.motorcade_name else r.motorcade_name end) as motorcade_name,
        r.happen_time, o.comment checked_comment, o.operator_name checker_name,
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
      ?: listOf(Approved.value(), Rejected.value())
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
    val po = repository.findById(id)
    return if (po.isPresent) Mono.just(po.get()) else Mono.empty()
  }

  override fun createBy(accidentDraft: AccidentDraft): Mono<AccidentRegister> {
    return bcDao.getCaseRelatedInfo(
      carPlate = accidentDraft.carPlate,
      driverName = accidentDraft.driverName,
      date = accidentDraft.happenTime.toLocalDate()
    ).map {
      val now = OffsetDateTime.now()
      val accidentRegister = AccidentRegister(
        // 基本信息
        status = Status.Draft,
        id = accidentDraft.id,

        // 复制信息
        happenTime = accidentDraft.happenTime,
        carPlate = accidentDraft.carPlate,
        driverName = accidentDraft.driverName,
        location = accidentDraft.location,
        hitForm = accidentDraft.hitForm,
        hitType = accidentDraft.hitType,
        describe = accidentDraft.describe,

        // 自动解析事发地点的省级、地级、县级 TODO
        locationLevel1 = null,
        locationLevel2 = null,
        locationLevel3 = null,

        // 自动匹配的车辆信息
        carId = it.carId,
        motorcadeName = it.motorcadeName,
        carModel = it.carModel,
        carContractType = it.contractType,
        carContractDrivers = it.contractDrivers,
        carOperateDate = it.carOperateDate,

        // 自动匹配的司机信息
        driverId = it.driverId,
        driverPicId = "S:${it.driverUid}",
        driverType = it.driverType,
        driverPhone = it.driverPhone,
        driverOrigin = it.driverOrigin,
        driverLinkmanName = it.relatedDriverName,
        driverLinkmanPhone = it.relatedDriverPhone,
        driverHiredDate = it.driverHiredDate,
        driverLicenseDate = it.driverLicenseDate,
        driverDriveYears = it.driverLicenseDate?.let { calculateYears(it, accidentDraft.happenTime).toBigDecimal() },
        driverAge = it.driverBirthDate?.let { calculateYears(it, accidentDraft.happenTime).toBigDecimal() },
        driverIdentityCode = it.driverIdentityCode,
        driverServiceCode = it.driverServiceCode,

        // 历史统计信息
        historyComplainCount = it.historyComplainCount,
        historyServiceOffenceCount = it.historyServiceOffenceCount,
        historyTrafficOffenceCount = it.historyTrafficOffenceCount,
        historyAccidentCount = it.historyAccidentCount
      )

      // 自动创建一条自车类型的当事车辆信息
      accidentRegister.cars = setOf(AccidentCar(
        sn = 0,
        parent = accidentRegister,
        name = accidentRegister.carPlate,
        type = "自车",
        updatedTime = now
      ))

      // 自动创建一条自车类型的当事人信息
      accidentRegister.peoples = setOf(AccidentPeople(
        sn = 0,
        parent = accidentRegister,
        name = accidentRegister.driverName,
        type = "自车",
        updatedTime = now
      ))

      accidentRegister.others = setOf()
      repository.save(accidentRegister)
    }
  }

  override fun getStatus(id: Int): Mono<Status> {
    return try {
      Mono.just(em.createQuery("select status from AccidentRegister where id = :id", Status::class.java)
        .setParameter("id", id)
        .singleResult)
    } catch (e: Exception) {
      if (e is NoResultException || e.cause is NoResultException) Mono.empty()
      else Mono.error(e)
    }
  }

  override fun toCheck(id: Int): Mono<Boolean> {
    try {
      // 获取状态、事发时间、首次提交时间
      val data = em.createQuery("select status, happenTime, registerTime from AccidentRegister where id = :id", Array<Any>::class.java)
        .setParameter("id", id)
        .singleResult
      val status = data[0] as Status
      if (status != Draft && status != Rejected) return Mono.just(false)
      val happenTime = data[1] as OffsetDateTime
      val registerTime = data[2] as OffsetDateTime?


      // 更新状态
      val query: Query
      if (registerTime == null) { // 首次提交
        val now = OffsetDateTime.now()
        val overdue = isOverdue(happenTime, now, overdueSeconds)
        query = em.createQuery(
          """update AccidentRegister set status = :status,
               overdue = :overdue,
               registerTime = :registerTime
               where id = :id and status <> :status""".trimIndent()
        ).setParameter("id", id)
          .setParameter("status", ToCheck)
          .setParameter("overdue", overdue)
          .setParameter("registerTime", now)
      } else {                    // 审核不通过后的再次提交
        query = em.createQuery(
          "update AccidentRegister set status = :status where id = :id and status <> :status"
        ).setParameter("id", id)
          .setParameter("status", ToCheck)
      }
      return if (query.executeUpdate() > 0) Mono.just(true) else Mono.just(false)
    } catch (e: Exception) {
      return if (e is NoResultException || e.cause is NoResultException) Mono.just(false) // 案件不存在
      else Mono.error(e)
    }
  }

  override fun checked(id: Int, passed: Boolean): Mono<Boolean> {
    val query = em.createQuery(
      "update AccidentRegister set status = :toStatus where id = :id and status = :limitedStatus"
    ).setParameter("id", id)
      .setParameter("toStatus", if (passed) Approved else Rejected)
      .setParameter("limitedStatus", ToCheck)
    return if (query.executeUpdate() > 0) Mono.just(true) else Mono.just(false)
  }

  val nestedPropertyKeys = listOf("cars", "peoples", "others")

  @Suppress("UNCHECKED_CAST")
  override fun update(id: Int, data: Map<String, Any?>): Mono<Boolean> {
    if (data.isEmpty()) return Mono.just(false)

    // 更新主体属性
    val main = data.filterKeys { !nestedPropertyKeys.contains(it) }
    val mainUpdatedSuccess = if (main.isNotEmpty()) {
      val ql = """|update AccidentRegister
                |  set ${data.keys.joinToString(",\n|  ") { "$it = :$it" }}
                |  where id = :id""".trimMargin()
      if (logger.isDebugEnabled) {
        logger.debug("ql={}", ql)
        logger.debug("id={}, data={}", id, data)
      }
      val query = em.createQuery(ql).setParameter("id", id)
      main.keys.forEach { query.setParameter(it, data[it]) }
      query.executeUpdate() > 0
    } else true

    // 更新当事车辆信息
    val cars = data["cars"] as List<AccidentCarDto4Update>?
    val carUpdatedSuccess = cars?.let {
      if (cars.isEmpty()) { // 清空现有数据
        em.createQuery("delete from AccidentCar where parent.id = :pid")
          .setParameter("pid", id)
          .executeUpdate() > 0
      } else {              // 增删改的处理
        // 获取现有数据
        val exists = em.createQuery("select t from AccidentCar t where t.parent.id = :pid", AccidentCar::class.java)
          .setParameter("pid", id)
          .resultList//.associate { it.id!! to it }
        // 删除多出的元素
        val toDeleteItems = exists.filterNot { po -> cars.any { dto -> dto.id == po.id } }
        val deleteSuccess = if (toDeleteItems.isNotEmpty()) {
          em.createQuery("delete from AccidentCar where id in (:ids)")
            .setParameter("ids", toDeleteItems.map { it.id }.toList())
            .executeUpdate() > 0
        } else true

        // 处理新增的元素
        val toCreateItems = cars.filter { it.id == null }
        val createSuccess = if (toCreateItems.isNotEmpty()) {
          val register = em.find(AccidentRegister::class.java, id)
          toCreateItems.forEach {
            em.persist(AccidentCar(
              parent = register,
              sn = it.sn!!,
              name = it.name!!,
              type = it.type!!,
              model = it.model,
              towCount = it.towCount,
              towMoney = it.towMoney,
              repairType = it.repairType,
              repairMoney = it.repairMoney,
              damageState = it.damageState,
              damageMoney = it.damageMoney,
              followType = it.followType,
              updatedTime = OffsetDateTime.now()
            ))
          }
          true
        } else true

        // 处理要更新的元素
        val toUpdateItems = cars.filter { it.id != null && it.data.size > 1 }
        val updateSuccess = if (toUpdateItems.isNotEmpty()) {
          var success = true
          toUpdateItems.forEach {
            val itemDate = it.data.filterNot { it.key == "id" || it.key == "updateTime" }
            val ql = """|update AccidentCar
                |  set ${itemDate.keys.joinToString(",\n|  ") { "$it = :$it" }}
                |  where id = :id""".trimMargin()
            if (logger.isDebugEnabled) {
              logger.debug("update AccidentCar: ql={}", ql)
              logger.debug("update AccidentCar: id={}, data={}", it.id, itemDate)
            }
            val query = em.createQuery(ql).setParameter("id", it.id)
            itemDate.keys.forEach { query.setParameter(it, itemDate[it]) }
            success = success && query.executeUpdate() > 0
          }
          success
        } else true

        deleteSuccess && createSuccess && updateSuccess
      }
    } ?: true

    // 更新当事人信息 TODO

    // 更新其他物体信息 TODO

    return Mono.just(mainUpdatedSuccess && carUpdatedSuccess)
  }
}