package cn.gftaxi.traffic.accident.dao.jpa.repository

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.po.AccidentSituation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * 案件当前处理情况 Repository 接口。
 *
 * @author RJ
 */
interface AccidentSituationJpaRepository : JpaRepository<AccidentSituation, Int> {
  @Query("select draftStatus from AccidentSituation where id = ?1")
  fun getDraftStatus(id: Int): List<DraftStatus>

  @Query("select registerStatus from AccidentSituation where id = ?1")
  fun getRegisterStatus(id: Int): List<AuditStatus>

  @Query("select reportStatus from AccidentSituation where id = ?1")
  fun getReportStatus(id: Int): List<AuditStatus>
}