package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_CHECK
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.Utils.ACCIDENT_REPORT_TARGET_TYPE
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * 事故报告 [AccidentReportService] 实现。
 *
 * @author zh
 * @author RJ
 */
@Service
@Transactional
class AccidentReportServiceImpl @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao
) : AccidentReportService {
  override fun find(pageNo: Int, pageSize: Int, reportStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentReportDto4View>> {
    return securityService.verifyHasAnyRole(*ROLES_REPORT_READ)
      .then(Mono.fromSupplier { accidentDao.findReport(pageNo, pageSize, reportStatuses, search) }.flatMap { it })
  }

  override fun get(id: Int): Mono<AccidentReportDto4Form> {
    return securityService.verifyHasAnyRole(*ROLES_REPORT_READ)
      .then(Mono.fromSupplier { accidentDao.getReport(id) }.flatMap { it })
  }

  override fun update(id: Int, dataDto: AccidentReportDto4FormUpdate): Mono<Void> {
    return accidentDao
      // 1. 获取登记状态
      .getReportStatus(id)
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // 2. 验证权限
      .flatMap {
        if (it == AuditStatus.ToSubmit)
          securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT, ROLE_REPORT_MODIFY)
        else
          securityService.verifyHasAnyRole(ROLE_REPORT_MODIFY)
      }
      // 3. 执行更新
      .then(Mono.fromSupplier {
        accidentDao.update(
          id = id,
          data = dataDto.data,
          targetType = ACCIDENT_REPORT_TARGET_TYPE,
          generateLog = true
        )
      }.flatMap { it })
  }

  override fun toCheck(id: Int): Mono<Void> {
    return securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT)
      // 1. 获取登记状态
      .then(Mono.fromSupplier { accidentDao.getReportStatus(id) }.flatMap { it })
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      .map {
        // 3. 案件状态不恰当
        if (it != AuditStatus.ToSubmit && it != AuditStatus.Rejected)
          throw ForbiddenException("案件不是待提交或审核不通过状态，不能提交审核！id=$id")
        else it
      }
      .flatMap {
        // 4. 更新案件状态
        accidentDao.update(
          id = id,
          data = when (it) {
            // 首次提交
            AuditStatus.ToSubmit -> mapOf(
              "stage" to CaseStage.Reporting,
              "reportStatus" to AuditStatus.ToCheck
            )
            // 非首次提交
            else -> mapOf("reportStatus" to AuditStatus.ToCheck)
          },
          targetType = ACCIDENT_REPORT_TARGET_TYPE,
          generateLog = false
        )
      }
      // 5. 生成提交日志 TODO
      .then()
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
      // 5. 生成审核日志 TODO
      .then()
  }
}