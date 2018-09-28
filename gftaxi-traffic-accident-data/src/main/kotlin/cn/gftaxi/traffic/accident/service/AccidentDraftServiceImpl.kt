package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_DRAFT_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_SUBMIT
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils
import cn.gftaxi.traffic.accident.common.Utils.ACCIDENT_DRAFT_TARGET_TYPE
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4View
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime

/**
 * 事故报案 Service 实现。
 *
 * @author JF
 */
@Service
@Transactional
class AccidentDraftServiceImpl @Autowired constructor(
  @Value("\${app.draft-overdue-hours:12}") private val overdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao
) : AccidentDraftService {
  private val overdueSeconds = overdueHours * 60 * 60
  override fun find(pageNo: Int, pageSize: Int, draftStatuses: List<DraftStatus>?, search: String?)
    : Mono<Page<AccidentDraftDto4View>> {
    return securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)
      .then(Mono.fromSupplier { accidentDao.findDraft(pageNo, pageSize, draftStatuses, search) }.flatMap { it })
  }

  override fun get(id: Int): Mono<AccidentDraftDto4Form> {
    return securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)
      .then(Mono.fromSupplier { accidentDao.getDraft(id) }.flatMap { it })
  }

  override fun submit(dto: AccidentDraftDto4Form): Mono<Pair<AccidentCase, AccidentSituation>> {
    return securityService
      // 1. 验证权限
      .verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
      // 2. 验证车号+事发时间的案件不应存在
      .then(Mono.fromSupplier { accidentDao.verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!) }.flatMap { it })
      // 3. 创建新的报案
      .then(
        Mono.fromSupplier {
          accidentDao.createCase(dto.apply {
            if (draftTime == null) draftTime = OffsetDateTime.now()
            if (overdueDraft == null) overdueDraft = Utils.isOverdue(happenTime!!, draftTime!!, overdueSeconds)
          })
        }.flatMap { it }
      )
      // 4.生成上报日志 TODO
      .doOnNext { }
  }

  override fun update(id: Int, dataDto: AccidentDraftDto4FormUpdate): Mono<Void> {
    return accidentDao
      // 1. 获取案件状态
      .getDraftStatus(id)
      // 2. 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // 2. 验证权限
      .flatMap {
        if (it == DraftStatus.ToSubmit)
          securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY)
        else
          securityService.verifyHasAnyRole(ROLE_DRAFT_MODIFY)
      }
      // 3. 执行更新
      .then(Mono.fromSupplier {
        accidentDao.update(
          id = id,
          data = dataDto.data,
          targetType = ACCIDENT_DRAFT_TARGET_TYPE,
          generateLog = true
        )
      }.flatMap { it })
  }
}