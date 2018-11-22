package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_DRAFT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.CREATION_NOT_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CREATION_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_DRAFT_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_MODIFY
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_SUBMIT
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.calculateOverdueDayAndHour
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormSubmit
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
import tech.simter.operation.OperationType
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Target
import tech.simter.operation.service.OperationService
import tech.simter.reactive.context.SystemContext.User
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

/**
 * 事故报案 Service 实现。
 *
 * @author RJ
 * @author zh
 */
@Service
@Transactional
class AccidentDraftServiceImpl @Autowired constructor(
  @Value("\${app.draft-overdue-hours:12}") private val draftOverdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val operationService: OperationService
) : AccidentDraftService {
  private val draftOverdueSeconds = draftOverdueHours * 60 * 60
  override fun find(pageNo: Int, pageSize: Int, draftStatuses: List<DraftStatus>?, search: String?)
    : Mono<Page<AccidentDraftDto4View>> {
    return securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)
      .then(Mono.defer { accidentDao.findDraft(pageNo, pageSize, draftStatuses, search) })
  }

  override fun get(id: Int): Mono<AccidentDraftDto4Form> {
    return securityService.verifyHasAnyRole(*ROLES_DRAFT_READ)
      .then(Mono.defer { accidentDao.getDraft(id) })
  }

  override fun submit(dto: AccidentDraftDto4FormSubmit): Mono<Pair<AccidentCase, AccidentSituation>> {
    return securityService
      // 1. 验证权限
      .verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
      // 2. 验证车号+事发时间的案件不应存在
      .then(Mono.defer { accidentDao.verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!) })
      // 3. 创建新的报案
      .then(Mono.defer { accidentDao.createCase(dto) })
      // 4.生成上报日志
      .flatMap { pair ->
        securityService.getAuthenticatedUser()
          .map(Optional<User>::get)
          .flatMap {
            operationService.create(Operation(
              time = OffsetDateTime.now(),
              type = OperationType.Creation.name,
              target = Target(id = pair.first.id.toString(), type = ACCIDENT_DRAFT_TARGET_TYPE),
              operator = it.toOperator(),
              cluster = "$ACCIDENT_OPERATION_CLUSTER-${pair.first.id}",
              result = calculateOverdueDayAndHour(pair.first.happenTime!!,
                pair.second.draftTime!!, draftOverdueSeconds).let {
                if (it == "") CREATION_NOT_OVERDUE_RESUTLT
                else CREATION_OVERDUE_RESUTLT + it
              },
              title = operationTitles[OperationType.Creation.name + ACCIDENT_DRAFT_TARGET_TYPE]!!
            ))
          }.then(Mono.just(pair))
      }
  }

  override fun update(id: Int, dataDto: AccidentDraftDto4FormUpdate): Mono<Void> {
    return accidentDao
      // 1. 获取案件状态
      .getDraftStatus(id)
      // 2.1 案件不存在
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在！id=$id")))
      // 2.2 案件存在，验证权限
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
