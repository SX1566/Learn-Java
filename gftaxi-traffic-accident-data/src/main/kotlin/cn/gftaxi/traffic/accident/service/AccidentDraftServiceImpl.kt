package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.security.SecurityService
import java.util.concurrent.TimeUnit

/**
 * 事故报案 Service 实现。
 *
 * @author JF
 */
@Component
@Transactional
class AccidentDraftServiceImpl @Autowired constructor(
  private val securityService: SecurityService,
  private val accidentDraftDao: AccidentDraftDao
) : AccidentDraftService {
  override fun find(pageNo: Int, pageSize: Int, status: Status?, fuzzySearch: String?): Mono<Page<AccidentDraft>> {
    securityService.verifyHasRole(AccidentDraft.ROLE_READ)
    return accidentDraftDao.find(pageNo, pageSize, status, fuzzySearch)
  }

  override fun findTodo(): Flux<AccidentDraft> {
    securityService.verifyHasRole(AccidentDraft.ROLE_READ)
    return accidentDraftDao.findTodo()
  }

  override fun get(code: String): Mono<AccidentDraft> {
    securityService.verifyHasRole(AccidentDraft.ROLE_READ)
    return accidentDraftDao.get(code)
  }

  override fun submit(dto: AccidentDraftDto4Submit): Mono<String> {
    securityService.verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    return accidentDraftDao
      .nextCode(dto.happenTime)
      .flatMap {
        accidentDraftDao
          .create(
            AccidentDraft(
              it, Status.Done, dto.carPlate, dto.driverName, dto.happenTime, dto.reportTime, dto.location, dto.hitForm,
              dto.hitType, AccidentDraft.isOverdue(dto.happenTime, dto.reportTime, TimeUnit.HOURS.toSeconds(12)),
              dto.source, dto.authorName, dto.authorId, dto.describe
            )
          )
          .thenReturn(it)
      }
  }

  override fun modify(code: String, dto: AccidentDraftDto4Modify): Mono<Void> {
    securityService.verifyHasRole(AccidentDraft.ROLE_MODIFY)
    val data = mapOf("carPlate" to dto.carPlate, "driverName" to dto.driverName, "happenTime" to dto.happenTime
      , "location" to dto.location, "hitForm" to dto.hitForm, "hitType" to dto.hitType, "describe" to dto.describe)
    return accidentDraftDao
      .update(code, data)
      .flatMap {
        if (it) Mono.empty<Void>()
        else Mono.error(IllegalArgumentException("指定的案件编号不存在"))
      }
  }
}