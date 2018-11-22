package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_REPORT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.APPROVAL_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_NOT_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.REJECTION_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_CHECK
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.Utils.calculateOverdueDayAndHour
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.operation.OperationType.*
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Target
import tech.simter.operation.service.OperationService
import tech.simter.reactive.context.SystemContext.User
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

/**
 * 事故报告 [AccidentReportService] 实现。
 *
 * @author zh
 * @author RJ
 */
@Service
@Transactional
class AccidentReportServiceImpl @Autowired constructor(
  @Value("\${app.report-overdue-hours:48}") private val reportOverdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val operationService: OperationService
) : AccidentReportService {
  private val reportOverdueSeconds = reportOverdueHours * 60 * 60
  override fun find(pageNo: Int, pageSize: Int, reportStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentReportDto4View>> {
    return securityService.verifyHasAnyRole(*ROLES_REPORT_READ)
      .then(Mono.defer { accidentDao.findReport(pageNo, pageSize, reportStatuses, search) })
  }

  override fun get(id: Int): Mono<AccidentReportDto4Form> {
    return securityService.verifyHasAnyRole(*ROLES_REPORT_READ)
      .then(Mono.defer { accidentDao.getReport(id) })
  }

  override fun update(id: Int, dataDto: AccidentReportDto4FormUpdate): Mono<Void> {
    return accidentDao
      // 1. 获取登记状态
      .getReportStatus(id)
      // 2.1 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // 2.2 验证权限
      .flatMap {
        if (listOf(AuditStatus.ToSubmit, AuditStatus.Rejected).contains(it))
          securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)
        else
          securityService.verifyHasAnyRole(ROLE_REPORT_MODIFY)
      }
      // 3. 执行更新
      .then(Mono.defer {
        accidentDao.update(
          id = id,
          data = dataDto.data,
          targetType = ACCIDENT_REPORT_TARGET_TYPE,
          generateLog = true
        )
      })
  }

  override fun toCheck(id: Int): Mono<Void> {
    val now = OffsetDateTime.now()
    return securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT)
      // 1. 获取登记状态
      .then(Mono.defer { accidentDao.getCaseSituation(id) })
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      .map {
        // 3. 案件状态不恰当
        val reportStatus = it.second.reportStatus
        if (reportStatus != AuditStatus.ToSubmit && reportStatus != AuditStatus.Rejected)
          throw ForbiddenException("案件不是待提交或审核不通过状态，不能提交审核！id=$id")
        else it
      }
      .flatMap {
        // 4. 更新案件状态
        accidentDao.update(
          id = id,
          data = when (it.second.reportStatus) {
          // 首次提交
            AuditStatus.ToSubmit -> mapOf(
              "stage" to CaseStage.Reporting,
              "reportStatus" to AuditStatus.ToCheck,
              "reportTime" to now,
              "overdueReport" to isOverdue(it.first.happenTime!!, now, reportOverdueSeconds)
            )
          // 非首次提交
            else -> mapOf("reportStatus" to AuditStatus.ToCheck)
          },
          targetType = ACCIDENT_REPORT_TARGET_TYPE,
          generateLog = false
        )
          // 5. 首次提交下，计算逾期的时间
          .then(Mono.just(
            if (it.second.reportStatus == AuditStatus.ToSubmit) {
              Optional.ofNullable(calculateOverdueDayAndHour(it.first.happenTime!!, now, reportOverdueSeconds))
            } else Optional.ofNullable(null)
          ))
      }
      // 6. 生成提交日志
      .flatMap { overdueDayAndHour ->
        securityService.getAuthenticatedUser()
          .map(Optional<User>::get)
          .flatMap {
            operationService.create(Operation(
              time = now,
              type = Confirmation.name,
              target = Target(id = id.toString(), type = ACCIDENT_REPORT_TARGET_TYPE),
              operator = it.toOperator(),
              cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
              result = overdueDayAndHour.map {
                if (it == "") CONFIRMATION_NOT_OVERDUE_RESUTLT
                else CONFIRMATION_OVERDUE_RESUTLT + it
              }.orElse(null),
              title = operationTitles[Confirmation.name + ACCIDENT_REPORT_TARGET_TYPE]!!
            ))
          }
      }
  }

  override fun checked(id: Int, checkedInfo: CheckedInfoDto): Mono<Void> {
    return securityService.verifyHasAnyRole(ROLE_REPORT_CHECK)
      // 1. 获取事故登记状态
      .then(Mono.defer { accidentDao.getSituation(id) })
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      .map {
        // 3. 案件状态不恰当
        if (it.reportStatus != AuditStatus.ToCheck) throw ForbiddenException("案件不是待审核状态，不能审核！id=$id")
        else it
      }
      .flatMap {
        // 4. 更新案件状态
        accidentDao.update(
          id = id,
          data = when (checkedInfo.passed) {
          // 审核通过
            true -> mapOf(
              "reportStatus" to AuditStatus.Approved,
              "stage" to CaseStage.Following,
              "reportCheckedCount" to (it.reportCheckedCount ?: 0) + 1,
              "reportCheckedComment" to null,
              "reportCheckedAttachments" to null
            )
          // 审核不通过
            else -> mapOf(
              "reportStatus" to AuditStatus.Rejected,
              "reportCheckedCount" to (it.reportCheckedCount ?: 0) + 1,
              "reportCheckedComment" to checkedInfo.comment,
              "reportCheckedAttachments" to checkedInfo.attachment?.run { listOf(checkedInfo.attachment) }
            )
          },
          targetType = ACCIDENT_REPORT_TARGET_TYPE,
          generateLog = false
        )
      }
      // 5. 生成审核日志
      .then(Mono.defer {
        securityService.getAuthenticatedUser()
          .map(Optional<User>::get)
          .flatMap {
            val type = if (checkedInfo.passed) Approval.name else Rejection.name
            operationService.create(Operation(
              time = OffsetDateTime.now(),
              type = type,
              target = Target(id = id.toString(), type = ACCIDENT_REPORT_TARGET_TYPE),
              operator = it.toOperator(),
              cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
              result = if (checkedInfo.passed) APPROVAL_RESUTLT else REJECTION_RESUTLT,
              comment = checkedInfo.comment,
              attachments = checkedInfo.attachment?.let { listOf(it) },
              title = operationTitles[type + ACCIDENT_REPORT_TARGET_TYPE]!!
            ))
          }
      })
  }
}