package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.po.AccidentDraft
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
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

  override fun get(code: String): Mono<AccidentDraft> {
    return Mono.just(
      em.createQuery("select a from AccidentDraft a where code = :code", AccidentDraft::class.java)
        .setParameter("code", code)
        .singleResult
    )
  }

  override fun create(po: AccidentDraft): Mono<Void> {
    val isNotExists =
      em.createQuery("select 0 from AccidentDraft where carPlate = :carPlate and happenTime = :happenTime")
        .setParameter("carPlate", po.carPlate)
        .setParameter("happenTime", po.happenTime)
        .resultList.isEmpty()
    if (isNotExists) repository.save(po) else throw IllegalArgumentException("指定车号和事发时间的案件已经存在！")
    return Mono.empty()
  }

  override fun update(code: String, data: Map<String, Any?>): Mono<Boolean> {
    val filteredData = data.filterKeys { it.isNotEmpty() }.toMutableMap()
    if (filteredData.isEmpty()) return Mono.just(true)

    if (filteredData.containsKey("happenTime")) {
      filteredData["happenTime"] = (filteredData["happenTime"] as OffsetDateTime).truncatedTo(ChronoUnit.MINUTES)
    }

    var setQl = "\n  set "
    val filteredDataKeys = filteredData.keys.toList()
    filteredDataKeys.forEach { setQl += "$it = :$it, " }
    val updateQl = "update AccidentDraft" + setQl.dropLast(2) + "\nwhere code = :code"

    val updateQuery = em.createQuery(updateQl)
    filteredDataKeys.forEach { updateQuery.setParameter(it, filteredData[it]) }
    updateQuery.setParameter("code", code)

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
      val sn = preCodes[0].takeLast(2).toInt() + 1
      Mono.just("${ymd}_${if (sn < 10) "0$sn" else "$sn"}")
    }
  }
}