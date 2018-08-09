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
import java.time.OffsetDateTime

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
        .defaultIfEmpty(EMPTY_REGISTER)
        .flatMap {
          if (it == EMPTY_REGISTER) { // 自动生成草稿的事故登记信息
            accidentDraftDao.get(id)
              .defaultIfEmpty(EMPTY_DRAFT)
              .flatMap {
                // convert empty to error (RJ:找不到其它方法)
                if (it == EMPTY_DRAFT) Mono.error(NotFoundException("案件不存在：id=$id"))
                else Mono.just(it)
              }
              .flatMap { accidentRegisterDao.createBy(it) }
          } else Mono.just(it)
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

  companion object {
    private val EMPTY_DRAFT = AccidentDraft(
      status = AccidentDraft.Status.Todo,
      carPlate = "",
      driverName = "",
      happenTime = OffsetDateTime.now(),
      code = "",
      authorId = "",
      authorName = "",
      source = "",
      overdue = false,
      location = "",
      reportTime = OffsetDateTime.now()
    )
    private val EMPTY_REGISTER = AccidentRegister(
      status = Status.Draft,
      draft = EMPTY_DRAFT,
      carPlate = "",
      driverName = "",
      happenTime = OffsetDateTime.now(),
      locationOther = ""
    )
  }
}