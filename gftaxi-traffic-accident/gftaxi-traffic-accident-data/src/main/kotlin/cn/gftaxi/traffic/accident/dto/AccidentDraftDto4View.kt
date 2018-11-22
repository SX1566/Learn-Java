package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.po.converter.DraftStatusConverter
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故报案视图用 DTO。
 *
 * @author RJ
 * @author zh
 */
@Entity
data class AccidentDraftDto4View constructor(
  //-- 案件基本信息 --
  @Id val id: Int? = null,
  val code: String? = null,
  val motorcadeName: String? = null,
  val carPlate: String? = null,
  val driverName: String? = null,
  @Convert(converter = DriverTypeConverter::class)
  val driverType: DriverType? = null,
  val happenTime: OffsetDateTime? = null,
  val hitForm: String? = null,
  val hitType: String? = null,
  val location: String? = null,

  //-- 案件当前处理情况信息 --
  @Convert(converter = DraftStatusConverter::class)
  val draftStatus: DraftStatus? = null,
  val authorName: String? = null,
  val draftTime: OffsetDateTime? = null,
  val overdueDraft: Boolean? = null,
  val source: String? = null
)