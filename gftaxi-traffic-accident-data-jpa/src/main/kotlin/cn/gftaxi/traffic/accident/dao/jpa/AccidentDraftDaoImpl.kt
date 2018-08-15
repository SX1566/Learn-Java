package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentDraftJpaRepository
import cn.gftaxi.traffic.accident.po.AccidentDraft
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.PersistenceContext

/**
 * 事故报案 Dao 实现。
 *
 * @author JF
 */
@Component
class AccidentDraftDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val repository: AccidentDraftJpaRepository
) : AccidentDraftDao {
  override fun find(pageNo: Int, pageSize: Int, status: AccidentDraft.Status?, fuzzySearch: String?): Mono<Page<AccidentDraft>> {
    val hasStatus = null != status
    val hasFuzzySearch = null != fuzzySearch
    var rowsQl = "select a from AccidentDraft a where 0 = 0"
    var countQl = "select count(code) from AccidentDraft where 0 = 0"
    var whereQl = if (hasStatus) "\n  and status = :status" else ""
    if (hasFuzzySearch) whereQl += "\n  and (code like :search or carPlate like :search or driverName like :search)"
    countQl += whereQl
    rowsQl = "$rowsQl$whereQl\norder by reportTime desc"

    val rowsQuery = em.createQuery(rowsQl, AccidentDraft::class.java)
      .setFirstResult(tech.simter.data.Page.calculateOffset(pageNo, pageSize))
      .setMaxResults(tech.simter.data.Page.toValidCapacity(pageSize))
    val countQuery = em.createQuery(countQl)
    if (hasStatus) {
      rowsQuery.setParameter("status", status)
      countQuery.setParameter("status", status)
    }
    if (hasFuzzySearch) {
      val search = if (!fuzzySearch!!.contains("%")) "%$fuzzySearch%" else fuzzySearch
      rowsQuery.setParameter("search", search)
      countQuery.setParameter("search", search)
    }

    return Mono.just(
      PageImpl(
        rowsQuery.resultList as List<AccidentDraft>,
        PageRequest.of(pageNo - 1, pageSize),
        (countQuery.singleResult as Number).toLong()
      )
    )
  }

  override fun findTodo(): Flux<AccidentDraft> {
    return Flux.fromIterable(
      em.createQuery("select a from AccidentDraft a where status = :todoStatus order by reportTime desc", AccidentDraft::class.java)
        .setParameter("todoStatus", AccidentDraft.Status.Todo)
        .resultList
    )
  }

  override fun get(id: Int): Mono<AccidentDraft> {
    val po = repository.findById(id)
    return if (po.isPresent) Mono.just(po.get()) else Mono.empty()
  }

  override fun create(po: AccidentDraft): Mono<AccidentDraft> {
    return try {
      val code: String = em.createQuery(
        "select code from AccidentDraft where code = :code or (carPlate = :carPlate and happenTime = :happenTime)",
        String::class.java
      ).setParameter("code", po.code)
        .setParameter("carPlate", po.carPlate)
        .setParameter("happenTime", po.happenTime)
        .setMaxResults(1)
        .singleResult
      if (code == po.code) Mono.error(NonUniqueException("相同编号的案件已经存在！"))
      else Mono.error(NonUniqueException("指定车号和事发时间的案件已经存在！"))
    } catch (e: NoResultException) {
      Mono.just(repository.save(po))
    }
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Boolean> {
    val filteredData = data.filterKeys { it.isNotEmpty() }.toMutableMap()
    if (filteredData.isEmpty()) return Mono.just(true)

    if (filteredData.containsKey("happenTime")) {
      filteredData["happenTime"] = (filteredData["happenTime"] as OffsetDateTime).truncatedTo(ChronoUnit.MINUTES)
    }

    var setQl = "\n  set "
    val filteredDataKeys = filteredData.keys.toList()
    filteredDataKeys.forEach { setQl += "$it = :$it, " }
    val updateQl = "update AccidentDraft" + setQl.dropLast(2) + "\nwhere id = :id"

    val updateQuery = em.createQuery(updateQl)
    filteredDataKeys.forEach { updateQuery.setParameter(it, filteredData[it]) }
    updateQuery.setParameter("id", id)

    val count = updateQuery.executeUpdate()
    return if (count == 1) Mono.just(true) else Mono.just(false)
  }

  override fun nextCode(happenTime: OffsetDateTime): Mono<String> {
    val ymd = happenTime.format(FORMAT_TO_YYYYMMDD)
    val code = "${ymd}_%"
    val hql = "select code from AccidentDraft where code like :code order by code desc"
    val preCodes = em.createQuery(hql, String::class.java).setParameter("code", code).setMaxResults(1).resultList

    return if (preCodes.isEmpty()) Mono.just("${ymd}_01")
    else {
      val takeLastWhile = preCodes[0].takeLastWhile { it != '_' }
      val sn = takeLastWhile.toInt() + 1
      Mono.just("${ymd}_${if (sn < 10) "0$sn" else "$sn"}")
    }
  }
}