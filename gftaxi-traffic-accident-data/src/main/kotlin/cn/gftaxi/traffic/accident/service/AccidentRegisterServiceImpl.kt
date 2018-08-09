package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.Utils.convert
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.security.SecurityService

/**
 * 事故登记 Service 实现。
 *
 * @author RJ
 */
@Service
@Transactional
class AccidentRegisterServiceImpl @Autowired constructor(
  private val securityService: SecurityService,
  private val accidentRegisterDao: AccidentRegisterDao,
  private val accidentDraftDao: AccidentDraftDao
) : AccidentRegisterService {
  override fun statSummary(): Flux<AccidentRegisterDto4StatSummary> {
    return try {
      securityService.verifyHasAnyRole(*READ_ROLES)
      accidentRegisterDao.statSummary()
    } catch (e: SecurityException) {
      Flux.error(PermissionDeniedException(e.message ?: ""))
    }
  }

  override fun findTodo(status: Status?): Flux<AccidentRegisterDto4Todo> {
    return try {
      securityService.verifyHasAnyRole(*READ_ROLES)
      accidentRegisterDao.findTodo(status)
    } catch (e: SecurityException) {
      Flux.error(PermissionDeniedException(e.message ?: ""))
    }
  }

  override fun findChecked(pageNo: Int, pageSize: Int, status: Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4Checked>> {
    return try {
      securityService.verifyHasAnyRole(*READ_ROLES)
      accidentRegisterDao.findChecked(pageNo, pageSize, status, search)
    } catch (e: SecurityException) {
      Mono.error(PermissionDeniedException(e.message ?: ""))
    }
  }

  override fun get(id: Int): Mono<AccidentRegisterDto4Form> {
    return try {
      securityService.verifyHasAnyRole(*READ_ROLES)
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
    } catch (e: SecurityException) {
      Mono.error(PermissionDeniedException(e.message ?: ""))
    }
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Void> {
    TODO("not implemented")
  }

  override fun toCheck(id: Int): Mono<Void> {
    TODO("not implemented")
  }

  override fun checked(id: Int): Mono<Void> {
    TODO("not implemented")
  }
}