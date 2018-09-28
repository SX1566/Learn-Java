package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentCaseJpaRepository
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentSituationJpaRepository
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4View
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4View
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.simter.exception.NonUniqueException
import tech.simter.exception.NotFoundException
import java.time.OffsetDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故 Dao 实现。
 *
 * @author RJ
 */
@Component
@Transactional
class AccidentDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val caseRepository: AccidentCaseJpaRepository,
  private val situationRepository: AccidentSituationJpaRepository
) : AccidentDao {
  private val logger = LoggerFactory.getLogger(AccidentDaoImpl::class.java)
  override fun verifyCaseNotExists(carPlate: String, happenTime: OffsetDateTime): Mono<Void> {
    return if (caseRepository.existsByCarPlateAndHappenTime(carPlate, happenTime))
      Mono.error(NonUniqueException("相同车号和事发时间的案件已经存在！"))
    else Mono.empty()
  }

  override fun nextCaseCode(happenTime: OffsetDateTime): Mono<String> {
    val ymd = happenTime.format(FORMAT_TO_YYYYMMDD)
    return caseRepository.getMaxCode("${ymd}_").firstOrNull()?.run {
      val nextSn = takeLastWhile { it != '_' }.toInt() + 1
      Mono.just("${ymd}_${if (nextSn < 10) "0" else ""}$nextSn")
    } ?: Mono.just("${ymd}_01")
  }

  override fun createCase(caseData: AccidentDraftDto4Form): Mono<Pair<AccidentCase, AccidentSituation>> {
    TODO("not implemented")
  }

  override fun findDraft(pageNo: Int, pageSize: Int, draftStatuses: List<DraftStatus>?, search: String?)
    : Mono<Page<AccidentDraftDto4View>> {
    TODO("not implemented")
  }

  override fun getDraft(id: Int): Mono<AccidentDraftDto4Form> {
    TODO("not implemented")
  }

  override fun getDraftStatus(id: Int): Mono<DraftStatus> {
    return situationRepository.getDraftStatus(id).firstOrNull()?.run { toMono() } ?: Mono.empty()
  }

  override fun update(id: Int, data: Map<String, Any?>, targetType: String, generateLog: Boolean): Mono<Void> {
    if (data.isEmpty()) return Mono.empty()

    // 获取原始的数据
    val originCaseOptional = caseRepository.findById(id)
    if (!originCaseOptional.isPresent) return Mono.error(NotFoundException("案件不存在！id=$id"))
    val originCase = originCaseOptional.get()
    val originSituaction = situationRepository.findById(id).get()

    val ql = "update AccidentCase set " + data.keys.joinToString(", ") { "$it = :$it" } + " where id = :id"
    if (logger.isDebugEnabled) {
      logger.debug("id={}", id)
      logger.debug(data.toString())
      logger.debug(ql)
    }
    val query = em.createQuery(ql)
    data.keys.forEach { query.setParameter(it, data[it]) }
    query.setParameter("id", id)

    return Mono.empty()
  }

  override fun getCaseSituation(id: Int): Mono<Pair<AccidentCase, AccidentSituation>> {
    return caseRepository.findById(id).flatMap { case ->
      situationRepository.findById(id).map { Pair(case, it).toMono() }
    }.orElse(Mono.empty())
  }

  override fun getCase(id: Int): Mono<AccidentCase> {
    return caseRepository.findById(id).map { it.toMono() }.orElse(Mono.empty())
  }

  override fun getSituation(id: Int): Mono<AccidentSituation> {
    return situationRepository.findById(id).map { it.toMono() }.orElse(Mono.empty())
  }

  override fun findRegister(pageNo: Int, pageSize: Int, registerStatuses: List<AuditStatus>?, search: String?)
    : Mono<Page<AccidentRegisterDto4View>> {
    TODO("not implemented")
  }

  override fun getRegister(id: Int): Mono<AccidentRegisterDto4Form> {
    TODO("not implemented")
  }

  override fun getRegisterStatus(id: Int): Mono<AuditStatus> {
    return situationRepository.getRegisterStatus(id).firstOrNull()?.run { toMono() } ?: Mono.empty()
  }
}