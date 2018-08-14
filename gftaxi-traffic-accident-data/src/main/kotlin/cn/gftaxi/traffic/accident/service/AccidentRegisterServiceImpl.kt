package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.Utils.convert
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.*
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_CHECK
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_SUBMIT
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
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
  private val accidentRegisterDao: AccidentRegisterDao,
  private val accidentDraftDao: AccidentDraftDao,
  private val accidentOperationDao: AccidentOperationDao
) : AccidentRegisterService {
  override fun statSummary(): Flux<AccidentRegisterDto4StatSummary> {
    return securityService.verifyHasAnyRole(*READ_ROLES)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao.statSummary().collectList()
      }).flatMapIterable { it.asIterable() }
  }

  override fun findTodo(status: Status?): Flux<AccidentRegisterDto4Todo> {
    return securityService.verifyHasAnyRole(*READ_ROLES)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao.findTodo(status).collectList()
      }).flatMapIterable { it.asIterable() }
  }

  override fun findLastChecked(pageNo: Int, pageSize: Int, status: Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4LastChecked>> {
    return securityService.verifyHasAnyRole(*READ_ROLES)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao.findLastChecked(pageNo, pageSize, status, search)
      })
  }

  override fun get(id: Int): Mono<AccidentRegisterDto4Form> {
    return securityService.verifyHasAnyRole(*READ_ROLES)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao
          // 1. 获取事故登记信息
          .get(id)
          // 2. 如果事故报案信息还没有登记过，则自动根据未曾登记过的事故报案信息生成一条草稿状态的事故登记信息
          .transform {
            if (it == Mono.empty<AccidentRegister>()) {
              accidentDraftDao.get(id)
                .transform {
                  // convert empty to error
                  if (it == Mono.empty<AccidentDraft>()) Mono.error(NotFoundException("案件不存在：id=$id"))
                  else it
                }
                .flatMap { accidentRegisterDao.createBy(it) }
            } else it
          }
          // 3. po 转 dto
          .map { convert(it) }
      })
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Void> {
    TODO("not implemented")
  }

  override fun toCheck(id: Int): Mono<Void> {
    return securityService.verifyHasAnyRole(ROLE_SUBMIT)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao
          // 1. 获取事故登记信息的状态
          .getStatus(id)
          // 2. 如果案件不存在返回 NotFound 错误
          .transform {
            when (it) {
              Mono.empty<Status>() -> Mono.error(NotFoundException("案件不存在：id=$id"))
              else -> it
            }
          }
          // 3. 如果案件状态不对，返回 Forbidden 错误
          .map<Status> {
            if (it != Status.Draft && it != Status.Rejected)
              throw ForbiddenException("案件不是待登记或审核不通过状态：id=$id")
            else it
          }
          // 4. 提交案件
          .flatMap { accidentRegisterDao.toCheck(id) }
          // 5. 如果提交成功则创建一条操作日志
          .map {
            if (it) accidentOperationDao.create(
              operationType = Confirmation,
              targetType = TargetType.Register,
              targetId = id)
            else Mono.empty()
          }
          .then()
      })
  }

  override fun checked(id: Int, checkedInfo: CheckedInfo): Mono<Void> {
    return securityService.verifyHasAnyRole(ROLE_CHECK)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao
          // 1. 获取事故登记信息的状态
          .getStatus(id)
          // 2. 如果案件不存在返回 NotFound 错误
          .transform {
            when (it) {
              Mono.empty<Status>() -> Mono.error(NotFoundException("案件不存在：id=$id"))
              else -> it
            }
          }
          // 3. 如果案件状态不对，返回 Forbidden 错误
          .map<Status> {
            if (it != Status.ToCheck)
              throw ForbiddenException("案件不是待审核状态：id=$id")
            else it
          }
          // 4. 设置案件的审核状态
          .flatMap { accidentRegisterDao.checked(id, checkedInfo.passed) }
          // 5. 如果审核成功则创建一条审核日志
          .map {
            if (it) accidentOperationDao.create(
              operationType = if (checkedInfo.passed) Approval else Rejection,
              targetType = TargetType.Register,
              targetId = id,
              comment = checkedInfo.comment,
              attachmentId = checkedInfo.attachmentId,
              attachmentName = checkedInfo.attachmentName
            ) else Mono.empty()
          }
          .then()
      })
  }
}