package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.NotFoundException
import tech.simter.security.SecurityService
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
  private val securityService: SecurityService,
  private val accidentDraftDao: AccidentDraftDao,
  private val bcDao: BcDao
) : AccidentDraftService {
  private val overdueSeconds = overdueHours * 60 * 60
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
      .nextCode(dto.happenTime!!)
      .flatMap { code ->
        bcDao.getMotorcadeName(dto.carPlate!!, dto.happenTime!!.toLocalDate()).flatMap {
          if (dto.createTime == null) dto.createTime = OffsetDateTime.now() // 报案时间为当前时间
          accidentDraftDao.create(AccidentDraft(
            code = code,
            status = Status.Todo,
            motorcadeName = if (it.isEmpty()) null else it,
            carPlate = dto.carPlate!!,
            driverName = dto.driverName!!,
            happenTime = dto.happenTime!!,
            createTime = dto.createTime!!,
            location = dto.location!!,
            hitForm = dto.hitForm,
            hitType = dto.hitType,
            overdueCreate = AccidentDraft.isOverdue(dto.happenTime!!, dto.createTime!!, overdueSeconds),
            source = dto.source!!,
            authorName = dto.authorName!!,
            authorId = dto.authorId!!,
            describe = dto.describe
          )).map { Pair(it.id!!, it.code) }
        }
      }
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Void> {
    securityService.verifyHasRole(AccidentDraft.ROLE_MODIFY)
    val carPlate: String? = data["carPlate"] as? String
    val happenTime: OffsetDateTime? = data["happenTime"] as? OffsetDateTime
    return if (carPlate != null || happenTime != null) {
      // 分支1==>车牌或事发时间需更新
      accidentDraftDao.get(id).flatMap { accidentDraft ->
        val updatedCarPlate = carPlate ?: accidentDraft.carPlate
        val updatedHappenTime = happenTime ?: accidentDraft.happenTime
        bcDao.getMotorcadeName(updatedCarPlate, updatedHappenTime.toLocalDate()).map { motorcadeName ->
          val mutableMap =
            mutableMapOf<String, Any?>("motorcadeName" to if (motorcadeName.isEmpty()) null else motorcadeName)
          // 事发时间更新时，逾期才需要更新
          happenTime?.let {
            mutableMap.put("overdueCreate", AccidentDraft.isOverdue(happenTime, accidentDraft.createTime, overdueSeconds))
          }
          mutableMap
        }
      }.switchIfEmpty(Mono.error(NotFoundException("指定的案件不存在")))
    } else {
      // 分支2==>车牌和事发时间都不需更新
      Mono.fromSupplier { mutableMapOf<String, Any?>() }
    }.flatMap {
      it.putAll(data)
      accidentDraftDao.update(id, it)
    }.flatMap {
      if (it)
        Mono.empty<Void>()
      else
        Mono.error(NotFoundException("指定的案件不存在"))
    }
  }
}