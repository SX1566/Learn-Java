package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.po.converter.AuditStatusConverter
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import tech.simter.operation.po.Attachment
import tech.simter.operation.po.converter.AttachmentsConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记视图用 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4View constructor(
  @Id val id: Int? = null,
  val code: String? = null,
  /** 登记状态 */
  @Convert(converter = AuditStatusConverter::class)
  val registerStatus: AuditStatus? = null,

  val motorcadeName: String? = null,
  val carPlate: String? = null,
  val driverName: String? = null,
  @Convert(converter = DriverTypeConverter::class)
  val driverType: DriverType? = null,
  val happenTime: OffsetDateTime? = null,
  val hitForm: String? = null,
  val hitType: String? = null,
  val location: String? = null,

  val authorName: String? = null,
  val draftTime: OffsetDateTime? = null,
  val overdueDraft: Boolean? = null,

  val registerTime: OffsetDateTime? = null,
  val overdueRegister: Boolean? = null,

  /** 审核次数 */
  val checkedCount: Int? = 0,
  /** 最后一次审核的审核意见 */
  val checkedComment: String? = null,
  /** 最后一次审核的审核附件 */
  @Convert(converter = AttachmentsConverter::class)
  val checkedAttachments: List<Attachment>? = null
)