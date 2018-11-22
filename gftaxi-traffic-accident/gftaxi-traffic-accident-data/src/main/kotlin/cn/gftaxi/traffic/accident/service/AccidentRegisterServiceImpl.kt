package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_REGISTER_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.APPROVAL_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_NOT_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.REJECTION_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_CHECK
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.calculateOverdueDayAndHour
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4View
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
 * 事故登记 Service 实现。
 *
 * @author RJ
 * @author zh
 */
@Service
@Transactional
class AccidentRegisterServiceImpl @Autowired constructor(
  @Value("\${app.register-overdue-hours:24}")
  private val registerOverdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val operationService: OperationService
) : AccidentRegisterService {
  private val registerOverdueSeconds = registerOverdueHours * 60 * 60
  override fun find(pageNo: Int, pageSize: Int, registerStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentRegisterDto4View>> {
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .then(Mono.defer { accidentDao.findRegister(pageNo, pageSize, registerStatuses, search) })
  }

  override fun get(id: Int): Mono<AccidentRegisterDto4Form> {
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .then(Mono.defer { accidentDao.getRegister(id) })
  }

  override fun update(id: Int, dataDto: AccidentRegisterDto4FormUpdate): Mono<Void> {
    return accidentDao
      // 1. 获取登记状态
      .getRegisterStatus(id)
      // 2.1 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // 2.2 验证权限
      .flatMap {
        if (listOf(AuditStatus.ToSubmit, AuditStatus.Rejected).contains(it))
          securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)
        else
          securityService.verifyHasAnyRole(ROLE_REGISTER_MODIFY)
      }
      // 3. 执行更新
      .then(Mono.defer {
        accidentDao.update(
          id = id,
          data = dataDto.data,
          targetType = ACCIDENT_REGISTER_TARGET_TYPE,
          generateLog = true
        )
      })
  }

  override fun toCheck(id: Int): Mono<Void> {
    val now = OffsetDateTime.now()
    return securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
      // 1. 获取登记状态
      .then(Mono.defer { accidentDao.getCaseSituation(id) })
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      .map {
        // 3. 案件状态不恰当
        val registerStatus = it.second.registerStatus
        if (registerStatus != AuditStatus.ToSubmit && registerStatus != AuditStatus.Rejected)
          throw ForbiddenException("案件不是待提交或审核不通过状态，不能提交审核！id=$id")
        else it
      }
      .flatMap {
        // 4. 更新案件状态
        accidentDao.update(
          id = id,
          data = when (it.second.registerStatus) {
          // 首次提交
            AuditStatus.ToSubmit -> mapOf(
              "stage" to CaseStage.Registering,
              "draftStatus" to DraftStatus.Drafted,
              "registerStatus" to AuditStatus.ToCheck,
              "registerTime" to now,
              "overdueRegister" to isOverdue(it.first.happenTime!!, now, registerOverdueSeconds)
            )
          // 非首次提交
            else -> mapOf("registerStatus" to AuditStatus.ToCheck)
          },
          targetType = ACCIDENT_REGISTER_TARGET_TYPE,
          generateLog = false
        )
          // 5. 首次提交下，计算逾期的时间
          .then(Mono.just(
            if (it.second.registerStatus == AuditStatus.ToSubmit) {
              Optional.ofNullable(calculateOverdueDayAndHour(it.first.happenTime!!, now, registerOverdueSeconds))
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
              target = Target(id = id.toString(), type = ACCIDENT_REGISTER_TARGET_TYPE),
              operator = it.toOperator(),
              cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
              result = overdueDayAndHour.map {
                if (it == "") CONFIRMATION_NOT_OVERDUE_RESUTLT
                else CONFIRMATION_OVERDUE_RESUTLT + it
              }.orElse(null),
              title = operationTitles[Confirmation.name + ACCIDENT_REGISTER_TARGET_TYPE]!!
            ))
          }
      }
  }

  override fun checked(id: Int, checkedInfo: CheckedInfoDto): Mono<Void> {
    return securityService.verifyHasAnyRole(ROLE_REGISTER_CHECK)
      // 1. 获取事故登记状态
      .then(Mono.defer { accidentDao.getSituation(id) })
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      .map {
        // 3. 案件状态不恰当
        if (it.registerStatus != AuditStatus.ToCheck) throw ForbiddenException("案件不是待审核状态，不能审核！id=$id")
        else it
      }
      .flatMap {
        // 4. 更新案件状态
        accidentDao.update(
          id = id,
          data = when (checkedInfo.passed) {
          // 审核通过
            true -> mapOf(
              "registerStatus" to AuditStatus.Approved,
              "reportStatus" to AuditStatus.ToSubmit,
              "stage" to CaseStage.Following,
              "registerCheckedCount" to (it.registerCheckedCount ?: 0) + 1,
              "registerCheckedComment" to null,
              "registerCheckedAttachments" to null
            )
          // 审核不通过
            else -> mapOf(
              "registerStatus" to AuditStatus.Rejected,
              "registerCheckedCount" to (it.registerCheckedCount ?: 0) + 1,
              "registerCheckedComment" to checkedInfo.comment,
              "registerCheckedAttachments" to checkedInfo.attachment?.run { listOf(checkedInfo.attachment) }
            )
          },
          targetType = ACCIDENT_REGISTER_TARGET_TYPE,
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
              target = Target(id = id.toString(), type = ACCIDENT_REGISTER_TARGET_TYPE),
              operator = it.toOperator(),
              cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
              comment = checkedInfo.comment,
              attachments = checkedInfo.attachment?.let { listOf(it) },
              result = if (checkedInfo.passed) APPROVAL_RESUTLT else REJECTION_RESUTLT,
              title = operationTitles[type + ACCIDENT_REGISTER_TARGET_TYPE]!!
            ))
          }
      })
  }
}