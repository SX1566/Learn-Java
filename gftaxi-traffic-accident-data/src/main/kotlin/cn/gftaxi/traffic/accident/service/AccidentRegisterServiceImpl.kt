package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_CHECK
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.ACCIDENT_REGISTER_TARGET_TYPE
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4View
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
 * 事故登记 Service 实现。
 *
 * @author RJ
 */
@Service
@Transactional
class AccidentRegisterServiceImpl @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao
) : AccidentRegisterService {
  override fun find(pageNo: Int, pageSize: Int, registerStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentRegisterDto4View>> {
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .then(Mono.fromSupplier { accidentDao.findRegister(pageNo, pageSize, registerStatuses, search) }.flatMap { it })
  }

  override fun get(id: Int): Mono<AccidentRegisterDto4Form> {
    return securityService.verifyHasAnyRole(*ROLES_REGISTER_READ)
      .then(Mono.fromSupplier { accidentDao.getRegister(id) }.flatMap { it })
  }

  override fun update(id: Int, dataDto: AccidentRegisterDto4FormUpdate): Mono<Void> {
    return accidentDao
      // 1. 获取登记状态
      .getRegisterStatus(id)
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // 2. 验证权限
      .flatMap {
        if (it == AuditStatus.ToSubmit)
          securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY)
        else
          securityService.verifyHasAnyRole(ROLE_REGISTER_MODIFY)
      }
      // 3. 执行更新
      .then(Mono.fromSupplier {
        accidentDao.update(
          id = id,
          data = dataDto.data,
          targetType = ACCIDENT_REGISTER_TARGET_TYPE,
          generateLog = true
        )
      }.flatMap { it })
  }

  override fun toCheck(id: Int): Mono<Void> {
    return securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
      // 1. 获取登记状态
      .then(Mono.fromSupplier { accidentDao.getRegisterStatus(id) }.flatMap { it })
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
              "stage" to CaseStage.Registering,
              "draftStatus" to DraftStatus.Drafted,
              "registerStatus" to AuditStatus.ToCheck
            )
            // 非首次提交
            else -> mapOf("registerStatus" to AuditStatus.ToCheck)
          },
          targetType = ACCIDENT_REGISTER_TARGET_TYPE,
          generateLog = false
        )
      }
      // 5. 生成提交日志 TODO
      .then()
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
      // 5. 生成审核日志 TODO
      .then()
  }
}