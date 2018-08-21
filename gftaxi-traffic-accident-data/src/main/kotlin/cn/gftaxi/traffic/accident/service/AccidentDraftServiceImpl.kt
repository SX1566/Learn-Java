package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.security.SecurityService
import java.util.concurrent.TimeUnit

/**
 * 事故报案 Service 实现。
 *
 * @author JF
 */
@Service
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

  override fun get(id: Int): Mono<AccidentDraft> {
    securityService.verifyHasRole(AccidentDraft.ROLE_READ)
    return accidentDraftDao.get(id)
  }

  override fun submit(dto: AccidentDraftDto4Submit): Mono<Pair<Int, String>> {
    securityService.verifyHasRole(AccidentDraft.ROLE_SUBMIT)
    return accidentDraftDao
      .nextCode(dto.happenTime)
      .flatMap { code ->
        accidentDraftDao.create(AccidentDraft(
          code = code,
          status = Status.Todo,
          carPlate = dto.carPlate,
          driverName = dto.driverName,
          happenTime = dto.happenTime,
          reportTime = dto.reportTime,
          location = dto.location,
          hitForm = dto.hitForm,
          hitType = dto.hitType,
          overdue = AccidentDraft.isOverdue(dto.happenTime, dto.reportTime, TimeUnit.HOURS.toSeconds(12)),
          source = dto.source,
          authorName = dto.authorName,
          authorId = dto.authorId,
          describe = dto.describe
        )).map { Pair(it.id!!, it.code) }
      }
  }

  override fun modify(id: Int, dto: AccidentDraftDto4Modify): Mono<Void> {
    securityService.verifyHasRole(AccidentDraft.ROLE_MODIFY)
    val data = mapOf(
      "carPlate" to dto.carPlate,
      "driverName" to dto.driverName,
      "happenTime" to dto.happenTime,
      "location" to dto.location,
      "hitForm" to dto.hitForm,
      "hitType" to dto.hitType,
      "describe" to dto.describe
    )
    return accidentDraftDao
      .update(id, data)
      .flatMap {
        if (it) Mono.empty<Void>()
        else Mono.error(NotFoundException("指定的案件不存在"))
      }
  }
}