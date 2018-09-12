package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentReport.Status
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故报告视图 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentReportDto4View constructor(
  @Id val id: Int? = null,
  val code: String? = null,
  val status: Status? = null,
  val motorcadeName: String? = null,
  val carPlate: String? = null,
  /** 车辆车型，如"现代 BH7183MY" */
  val carModel: String? = null,
  val driverName: String? = null,
  @Convert(converter = DriverTypeConverter::class)
  val driverType: DriverType? = null,
  val happenTime: OffsetDateTime? = null,
  val location: String? = null,
  /** 事故等级 */
  val level: String? = null,
  /** 事故形态 */
  val hitForm: String? = null,
  /** 自车责任 */
  val duty: String? = null,
  /** 报案时间 */
  val createTime: OffsetDateTime? = null,
  val overdueCreate: Boolean? = null,
  /** 登记时间 */
  val registerTime: OffsetDateTime? = null,
  val overdueRegister: Boolean? = null,
  /** 报告时间 */
  val reportTime: OffsetDateTime? = null,
  val overdueReport: Boolean? = null,
  /** 约定司机回队时间 */
  val appointDriverReturnTime: OffsetDateTime? = null,
  /** 审核次数 */
  val checkedCount: Int? = 0,
  /** 审核不通过时的审核附件 ID */
  val attachmentId: String? = null
)