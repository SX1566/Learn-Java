package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.Utils.convert
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.*
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_CHECK
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_MODIFY
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
import tech.simter.exception.PermissionDeniedException
import tech.simter.reactive.context.SystemContext.User
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

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
  override fun statSummary(scopeType: ScopeType, from: Int?, to: Int?): Flux<AccidentRegisterDto4StatSummary> {
    return securityService.verifyHasAnyRole(*READ_ROLES)
      .then(Mono.just(0).flatMap {
        accidentRegisterDao.statSummary(scopeType, from, to).collectList()
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
                .flatMap { accidentRegisterDao.createBy(it).zipWith(Mono.just(it)) }
            } else it.zipWith(accidentDraftDao.get(id))
          }
          // 3. po 转 dto
          .map { convert(it.t1, it.t2) }
      })
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Void> {
    return securityService
      // 1. 获取权限
      .hasRole(ROLE_SUBMIT, ROLE_MODIFY)
      // 2. 获取状态
      .zipWith(accidentRegisterDao.getStatus(id))
      .switchIfEmpty(Mono.error(NotFoundException("案件不存在：id=$id")))
      .flatMap {
        // 3. 判断是否有修改权限
        if (it.t1.second || (it.t1.first && it.t2 == Status.Draft)) { // 有权限修改
          // 3.1 执行修改
          accidentRegisterDao.update(id, data)
        } else {                                                      // 无权限修改
          Mono.error(PermissionDeniedException(
            "status=${it.t2}, ROLE_SUBMIT=${it.t1.first}, ROLE_MODIFY=${it.t1.second}"))
        }
      }
      // 3.2 修改成功后创建一条修改日志
      .flatMap {
        if (it) {
          securityService.getAuthenticatedUser()
            .map(Optional<User>::get)
            .flatMap {
              accidentOperationDao.create(AccidentOperation(
                operationType = Modification,
                targetType = TargetType.Register,
                targetId = id,
                operateTime = OffsetDateTime.now(),
                operatorId = it.id,
                operatorName = it.name
              ))
            }
        } else Mono.empty()
      }.then()
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
          .flatMap {
            if (it) {
              securityService.getAuthenticatedUser()
                .map(Optional<User>::get)
                .flatMap {
                  accidentOperationDao.create(AccidentOperation(
                    operationType = Confirmation,
                    targetType = TargetType.Register,
                    targetId = id,
                    operateTime = OffsetDateTime.now(),
                    operatorId = it.id,
                    operatorName = it.name
                  ))
                }
            } else Mono.empty()
          }.then()
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
          .flatMap {
            if (it) {
              securityService.getAuthenticatedUser()
                .map(Optional<User>::get)
                .flatMap {
                  accidentOperationDao.create(AccidentOperation(
                    operationType = if (checkedInfo.passed) Approval else Rejection,
                    targetType = TargetType.Register,
                    targetId = id,
                    comment = checkedInfo.comment,
                    attachmentId = checkedInfo.attachmentId,
                    attachmentName = checkedInfo.attachmentName,
                    operateTime = OffsetDateTime.now(),
                    operatorId = it.id,
                    operatorName = it.name
                  ))
                }
            } else Mono.empty()
          }.then()
      })
  }
}