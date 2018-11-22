package cn.gftaxi.traffic.accident.dao.jpa.repository

import cn.gftaxi.traffic.accident.po.AccidentCase
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime

/**
 * 案件信息 Repository 接口。
 *
 * @author RJ
 */
interface AccidentCaseJpaRepository : JpaRepository<AccidentCase, Int> {
  fun existsByCarPlateAndHappenTime(carPlate: String, happenTime: OffsetDateTime): Boolean

  @Query("select code from AccidentCase where code like ?1% order by code desc")
  fun getMaxCode(codePrefix: String, pageable: Pageable = PageRequest.of(0, 1)): List<String>
}