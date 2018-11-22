package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.*
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.common.Utils.SUB_LIST_PROPERTY_KEYS
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.*
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.simter.exception.NonUniqueException
import tech.simter.exception.NotFoundException
import tech.simter.operation.OperationType.Modification
import tech.simter.operation.dao.OperationDao
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Target
import tech.simter.reactive.context.SystemContext
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Function
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故 Dao 实现。
 *
 * @author RJ
 * @author zh
 */
@Component
@Transactional
class AccidentDaoImpl @Autowired constructor(
  @Value("\${app.draft-overdue-hours:12}") private val draftOverdueHours: Long,
  @PersistenceContext private val em: EntityManager,
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository,
  private val bcDao: BcDao,
  private val operationDao: OperationDao,
  private val securityService: ReactiveSecurityService
) : AccidentDao {
  private val logger = LoggerFactory.getLogger(AccidentDaoImpl::class.java)
  private val draftOverdueSeconds = draftOverdueHours * 60 * 60
  override fun verifyCaseNotExists(carPlate: String, happenTime: OffsetDateTime): Mono<Void> {
    return if (caseRepository.existsByCarPlateAndHappenTime(carPlate, happenTime))
      Mono.error(NonUniqueException("相同车号和事发时间的案件已经存在！"))
    else Mono.empty()
  }

  override fun nextCaseCode(happenTime: OffsetDateTime): Mono<String> {
    val ymd = happenTime.format(FORMAT_TO_YYYYMMDD)
    return caseRepository.getMaxCode("${ymd}_").firstOrNull()?.run {
      val nextSn = takeLastWhile { it != '_' }.toInt() + 1
      Mono.just("${ymd}_${if (nextSn < 10) "0" else ""}$nextSn")
    } ?: Mono.just("${ymd}_01")
  }

  override fun createCase(caseData: AccidentDraftDto4FormSubmit): Mono<Pair<AccidentCase, AccidentSituation>> {
    val now = OffsetDateTime.now()

    // 1. dto 转为 po
    val pair = caseData.toCaseSituation().apply {
      // 自动设置案件状态
      second.stage = CaseStage.Drafting
      second.draftStatus = DraftStatus.Drafting
      second.registerStatus = AuditStatus.ToSubmit
      // 报案时间
      second.draftTime = now
      // 逾期报案
      second.overdueDraft = isOverdue(first.happenTime!!, second.draftTime!!, draftOverdueSeconds)
      // temp: 登记信息的审核次数
      second.registerCheckedCount = second.registerCheckedCount ?: 0
      // temp: 报告信息的审核次数
      second.reportCheckedCount = second.reportCheckedCount ?: 0
    }

    // 2. 生成案件编号
    return nextCaseCode(caseData.happenTime!!)
      .map { pair.first.code = it; pair }
      // 3. 从 BC 系统获取相应的车辆和司机信息
      .flatMap {
        val caseRelatedInfo = bcDao.getCaseRelatedInfo(carPlate = pair.first.carPlate!!,
          driverName = pair.first.driverName!!,
          date = pair.first.happenTime!!.toLocalDate()
        )
        caseRelatedInfo
          .map { info -> info.copyTo(pair.first); pair }
      }
      // 4. 自动生成自车当事车辆、当事人信息
      .map {
        pair.apply {
          pair.first.cars = listOf(AccidentCar().also { car ->
            car.parent = pair.first
            car.sn = 0
            car.type = "自车"
            car.model = "出租车"
            car.name = pair.first.carPlate
            car.updatedTime = now
          })
          pair.first.peoples = listOf(AccidentPeople().also { car ->
            car.parent = pair.first
            car.sn = 0
            car.type = "自车"
            car.name = pair.first.driverName
            car.sex = Sex.NotSet
            car.updatedTime = now
          })
        }
      }
      // 5. 保存
      .map {
        caseRepository.save(it.first)
        it.second.id = it.first.id
        situationRepository.save(it.second)
        it
      }
  }

  @Suppress("UNCHECKED_CAST")
  override fun findDraft(pageNo: Int, pageSize: Int, draftStatuses: List<DraftStatus>?, search: String?)
    : Mono<Page<AccidentDraftDto4View>> {
    // 连接的表 ql
    val tableQl = """
        from gf_accident_case as c
        inner join gf_accident_situation as s on c.id = s.id
      """.trimIndent()
    // 筛选条件的 ql
    val searchQl = search?.let {
      """
        and ( c.code like :search
          or c.car_plate like :search
          or c.driver_name like :search )
      """.trimIndent()
    } ?: ""
    val hasStatus = draftStatuses?.isNotEmpty() == true
    val statusesQl = if (hasStatus) "and s.draft_status in :draftStatuses" else ""
    val whereQl = "where 1 = 1 $searchQl $statusesQl"
    // 排序的 ql
    val orderQl = "order by c.happen_time desc"
    // 拼接完整的 ql
    val countQl = "select count(0) $tableQl $whereQl"
    val rowQl = """
      select c.id as id, c.code as code, c.motorcade_name as motorcade_name,
        c.car_plate as car_plate, c.driver_name as driver_name,
        c.driver_type as driver_type, c.happen_time as happen_time,
        c.hit_form as hit_form, c.hit_type as hit_type, c.location as location,
        s.draft_status as draft_status, s.author_name as author_name,
        s.draft_time as draft_time, s.overdue_draft as overdue_draft, s.source
      $tableQl $whereQl $orderQl
    """.trimIndent()

    // 生成Query
    val rowsQuery = em.createNativeQuery(rowQl, AccidentDraftDto4View::class.java)
      .setFirstResult(tech.simter.data.Page.calculateOffset(pageNo, pageSize))
      .setMaxResults(tech.simter.data.Page.toValidCapacity(pageSize))
    val countQuery = em.createNativeQuery(countQl)

    // 设置参数
    search?.let {
      val searchStr = if (it.contains("%")) it else "%$it%"
      rowsQuery.setParameter("search", searchStr)
      countQuery.setParameter("search", searchStr)
    }
    if (hasStatus) {
      rowsQuery.setParameter("draftStatuses", draftStatuses!!.map { it.value() })
      countQuery.setParameter("draftStatuses", draftStatuses.map { it.value() })
    }

    // 将查询结果返回
    return Mono.just(
      PageImpl(
        rowsQuery.resultList as List<AccidentDraftDto4View>,
        PageRequest.of(pageNo - 1, pageSize),
        (countQuery.singleResult as Number).toLong()
      )
    )
  }

  override fun getDraft(id: Int): Mono<AccidentDraftDto4Form> {
    return getCaseSituation(id).map { AccidentDraftDto4Form.from(it) }
  }

  override fun getDraftStatus(id: Int): Mono<DraftStatus> {
    return situationRepository.getDraftStatus(id).firstOrNull()?.run { toMono() } ?: Mono.empty()
  }

  /**
   * 事故当事车辆、当事人、其他物品信息更新封装。
   *
   * 更新成功返回 true，否则返回 false。
   *
   * @param[pid] 事故登记 ID
   * @param[dtoList] 事故当事车辆、当事人、其他物品信息更新 DTO 列表
   * @param[poList] 事故当事车辆、当事人、其他物品信息未更新 PO 列表
   * @param[poClazz] 事故当事车辆、当事人、其他物品信息的 PO 对应的类型信息
   * @param[dto2po] DTO 转 PO 函数
   */
  private fun <DTO : AccidentSubListBaseInfo, PO : IdEntity> updateSubList(
    pid: Int, dtoList: List<DTO>?, poList: List<PO>?, poClazz: Class<PO>,
    dto2po: Function<DTO, PO>): Boolean {
    return if (dtoList == null || dtoList.isEmpty()) { // 清空现有数据
      em.createQuery("delete from ${poClazz.simpleName} where parent.id = :pid")
        .setParameter("pid", pid)
        .executeUpdate() > 0
    } else { // 增删改的处理
      // 删除多出的元素
      val toDeleteItems = poList?.filterNot { po -> dtoList.any { dto -> dto.id == po.id } }
      val isDeleted = if (toDeleteItems != null && toDeleteItems.isNotEmpty()) {
        em.createQuery("delete from ${poClazz.simpleName} where id in (:ids)")
          .setParameter("ids", toDeleteItems.map { it.id }.toList())
          .executeUpdate() > 0
      } else false

      // 处理要更新的元素
      val toUpdateItems = dtoList.filter { it.id != null && it.data.size > 1 }
      val isUpdated = if (toUpdateItems.isNotEmpty()) {
        toUpdateItems.sumBy { dto ->
          val itemDate = dto.data.filterNot { it.key == "id" || it.key == "updateTime" }
          val ql = """|update ${poClazz.simpleName}
                |  set ${itemDate.keys.joinToString(",\n|  ") { "$it = :$it" }}
                |  where id = :id""".trimMargin()
          if (logger.isDebugEnabled) {
            logger.debug("update : ql={}", ql)
            logger.debug("update : id={}, data={}", dto.id, itemDate)
          }
          val query = em.createQuery(ql).setParameter("id", dto.id)
          itemDate.keys.forEach { query.setParameter(it, itemDate[it]) }
          query.executeUpdate()
        } > 0
      } else false

      // 处理新增的元素
      val toCreateItems = dtoList.filter { it.id == null }
      val isCreated = if (toCreateItems.isNotEmpty()) {
        toCreateItems.forEach {
          val po = dto2po.apply(it)
          em.persist(po)
          it.id = po.id
        }
        true
      } else false
      isDeleted || isCreated || isUpdated
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun update(id: Int, data: Map<String, Any?>, targetType: String, generateLog: Boolean): Mono<Void> {
    if (data.isEmpty()) return Mono.empty()
    // 获取原始的数据
    val originCaseOptional = caseRepository.findById(id)

    if (!originCaseOptional.isPresent) return Mono.error(NotFoundException("案件不存在！id=$id"))
    val originCase = originCaseOptional.get()
    val now = OffsetDateTime.now()

    // 1. 更新主体属性
    val main =
      data.filterKeys { !SUB_LIST_PROPERTY_KEYS.contains(it) && !AccidentSituation.propertieKeys.contains(it) }
    val mainIsUpdated = if (main.isNotEmpty()) {
      val ql = "update AccidentCase set " +
        main.keys.joinToString(", ") { "$it = :$it" } + " where id = :id"
      val query = em.createQuery(ql).setParameter("id", id)
      main.keys.forEach { query.setParameter(it, data[it]) }
      query.executeUpdate() > 0
    } else false

    // 更新案件当前处理情况信息
    val situation = data.filterKeys { AccidentSituation.propertieKeys.contains(it) }
    val situationUpdated = if (situation.isNotEmpty()) {
      val ql = "update AccidentSituation set " +
        situation.keys.joinToString(", ") { "$it = :$it" } + " where id = :id"
      val query = em.createQuery(ql).setParameter("id", id)
      situation.keys.forEach { query.setParameter(it, data[it]) }
      query.executeUpdate() > 0
    } else false

    // 2. 更新当事车辆信息
    val carIsUpdated = if (data.containsKey("cars")) {
      updateSubList(
        pid = id,
        dtoList = data["cars"] as List<AccidentCarDto4Form>?,
        poList = originCase.cars,
        poClazz = AccidentCar::class.java,
        dto2po = Function {
          AccidentCar.from(it).apply {
            updatedTime = now
            parent = originCase
          }
        }
      )
    } else false

    // 3. 更新当事人信息
    val peopleIsUpdated = if (data.containsKey("peoples")) {
      updateSubList(
        pid = id,
        dtoList = data["peoples"] as List<AccidentPeopleDto4Form>?,
        poList = originCase.peoples,
        poClazz = AccidentPeople::class.java,
        dto2po = Function {
          AccidentPeople.from(it).apply {
            updatedTime = now
            parent = originCase
          }
        }
      )
    } else false

    // 4. 更新其他物体信息
    val otherIsUpdated = if (data.containsKey("others")) {
      updateSubList(
        pid = id,
        dtoList = data["others"] as List<AccidentOtherDto4Form>?,
        poList = originCase.others,
        poClazz = AccidentOther::class.java,
        dto2po = Function {
          AccidentOther.from(it).apply {
            updatedTime = now
            parent = originCase
          }
        }
      )
    } else false

    // 5. 清除jpa缓存
    em.clear()

    // 6. 需要记录操作日志且真正更新了某些字段时，记录操作日志
    return if (generateLog && (mainIsUpdated || situationUpdated || carIsUpdated || peopleIsUpdated || otherIsUpdated)) {
      securityService.getAuthenticatedUser()
        .map(Optional<SystemContext.User>::get)
        .flatMap {
          operationDao.create(Operation(
            time = OffsetDateTime.now(),
            type = Modification.name,
            target = Target(id = id.toString(), type = targetType),
            operator = it.toOperator(),
            cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
            fields = originCase.toFields(data),
            title = operationTitles[Modification.name + targetType]!!
          ))
        }
    } else {
      Mono.empty()
    }
  }

  override fun getCaseSituation(id: Int): Mono<Pair<AccidentCase, AccidentSituation>> {
    return caseRepository.findById(id).flatMap { case ->
      situationRepository.findById(id).map { Pair(case, it).toMono() }
    }.orElse(Mono.empty())
  }

  override fun getCase(id: Int): Mono<AccidentCase> {
    return caseRepository.findById(id).map { it.toMono() }.orElse(Mono.empty())
  }

  override fun getSituation(id: Int): Mono<AccidentSituation> {
    return situationRepository.findById(id).map { it.toMono() }.orElse(Mono.empty())
  }

  @Suppress("UNCHECKED_CAST")
  override fun findRegister(pageNo: Int, pageSize: Int, registerStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentRegisterDto4View>> {
    // 连接的表 ql
    val tableQl = """
        from gf_accident_case as c
        inner join gf_accident_situation as s on c.id = s.id
      """.trimIndent()
    // 筛选条件的 ql
    val searchQl = search?.let {
      """
        and ( c.code like :search
          or c.car_plate like :search
          or c.driver_name like :search )
      """.trimIndent()
    } ?: ""
    val hasStatus = registerStatuses?.isNotEmpty() == true
    val statusesQl = if (hasStatus) "and s.register_status in :registerStatus" else ""
    val whereQl = "where 1 = 1 $searchQl $statusesQl"
    // 排序的 ql
    val orderQl = "order by c.happen_time desc"
    // 拼接完整的 ql
    val countQl = "select count(0) $tableQl $whereQl"
    val rowQl = """
      select c.id as id, c.code as code, c.motorcade_name as motorcade_name,
        c.car_plate as car_plate, c.driver_name as driver_name,
        c.driver_type as driver_type, c.happen_time as happen_time,
        c.hit_form as hit_form, c.hit_type as hit_type, c.location as location,
        s.author_name as author_name, s.draft_time as draft_time,
        s.overdue_draft as overdue_draft, s.register_status as register_status,
        s.register_time as register_time, s.overdue_register as overdue_register,
        s.register_checked_count as checked_count,
        s.register_checked_comment as checked_comment,
        s.register_checked_attachments as checked_attachments
      $tableQl $whereQl $orderQl
    """.trimIndent()

    // 生成Query
    val rowsQuery = em.createNativeQuery(rowQl, AccidentRegisterDto4View::class.java)
      .setFirstResult(tech.simter.data.Page.calculateOffset(pageNo, pageSize))
      .setMaxResults(tech.simter.data.Page.toValidCapacity(pageSize))
    val countQuery = em.createNativeQuery(countQl)

    // 设置参数
    search?.let {
      val searchStr = if (it.contains("%")) it else "%$it%"
      rowsQuery.setParameter("search", searchStr)
      countQuery.setParameter("search", searchStr)
    }
    if (hasStatus) {
      rowsQuery.setParameter("registerStatus", registerStatuses!!.map { it.value() })
      countQuery.setParameter("registerStatus", registerStatuses.map { it.value() })
    }

    // 将查询结果返回
    return Mono.just(
      PageImpl(
        rowsQuery.resultList as List<AccidentRegisterDto4View>,
        PageRequest.of(pageNo - 1, pageSize),
        (countQuery.singleResult as Number).toLong()
      )
    )
  }

  override fun getRegister(id: Int): Mono<AccidentRegisterDto4Form> {
    return getCaseSituation(id).map { AccidentRegisterDto4Form.from(it) }
  }

  override fun getRegisterStatus(id: Int): Mono<AuditStatus> {
    return situationRepository.getRegisterStatus(id).firstOrNull()?.run { toMono() } ?: Mono.empty()
  }

  @Suppress("UNCHECKED_CAST")
  override fun findReport(pageNo: Int, pageSize: Int, reportStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentReportDto4View>> {
    // 连接的表 ql
    val tableQl = """
        from gf_accident_case as c
        inner join gf_accident_situation as s on c.id = s.id
      """.trimIndent()
    // 筛选条件的 ql
    val searchQl = search?.let {
      """
        and ( c.code like :search
          or c.car_plate like :search
          or c.driver_name like :search )
      """.trimIndent()
    } ?: ""
    val hasStatus = reportStatuses?.isNotEmpty() == true
    val statusesQl = if (hasStatus) "and s.report_status in :reportStatuses" else ""
    val whereQl = "where s.register_status = :registerStatus $searchQl $statusesQl"
    // 排序的 ql
    val orderQl = "order by c.happen_time desc"
    // 拼接完整的 ql
    val countQl = "select count(0) $tableQl $whereQl"
    val rowQl = """
      with people(id, duty) as (
        select pid, duty from gf_accident_people where type = :peopleType
      )
      select c.id as id, c.code as code, c.motorcade_name as motorcade_name,
        c.car_plate as car_plate, c.driver_name as driver_name,
        c.driver_type as driver_type, c.happen_time as happen_time,
        c.hit_form as hit_form, c.location as location,
        c.car_model as car_model, c.level as level,
        c.appoint_driver_return_time as appoint_driver_return_time,
        people.duty, s.report_status as report_status,
        s.draft_time as draft_time, s.overdue_draft as overdue_draft,
        s.register_time as register_time, s.overdue_register as overdue_register,
        s.report_time as report_time, s.overdue_report as overdue_report,
        s.report_checked_count as checked_count,
        s.report_checked_comment as checked_comment,
        s.report_checked_attachments as checked_attachments
      $tableQl
      left join people on people.id = c.id
      $whereQl $orderQl
    """.trimIndent()

    // 生成Query
    val rowsQuery = em.createNativeQuery(rowQl, AccidentReportDto4View::class.java)
      .setFirstResult(tech.simter.data.Page.calculateOffset(pageNo, pageSize))
      .setMaxResults(tech.simter.data.Page.toValidCapacity(pageSize))
      .setParameter("peopleType", "自车")
      .setParameter("registerStatus", AuditStatus.Approved.value())
    val countQuery = em.createNativeQuery(countQl)
      .setParameter("registerStatus", AuditStatus.Approved.value())
    // 设置参数
    search?.let {
      val searchStr = if (it.contains("%")) it else "%$it%"
      rowsQuery.setParameter("search", searchStr)
      countQuery.setParameter("search", searchStr)
    }
    if (hasStatus) {
      rowsQuery.setParameter("reportStatuses", reportStatuses!!.map { it.value() })
      countQuery.setParameter("reportStatuses", reportStatuses.map { it.value() })
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

  override fun getReport(id: Int): Mono<AccidentReportDto4Form> {
    return getCaseSituation(id).map { AccidentReportDto4Form.from(it) }
  }

  override fun getReportStatus(id: Int): Mono<AuditStatus> {
    return situationRepository.getReportStatus(id).firstOrNull()?.run { toMono() } ?: Mono.empty()
  }
}