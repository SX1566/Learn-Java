package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记的待登记、待审核案件 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4Todo constructor(
  @Id val id: Int? = null,
  val code: String,
  val carPlate: String,
  val driverName: String,
  @Convert(converter = DriverTypeConverter::class)
  val driverType: DriverType? = null, // 未登记的案件该值未知
  val happenTime: OffsetDateTime,
  val hitForm: String,
  val hitType: String,
  val location: String,
  val authorName: String,
  val authorId: String,
  val reportTime: OffsetDateTime,
  val overdueReport: Boolean,
  val registerTime: OffsetDateTime? = null,
  val overdueRegister: Boolean? = null,
  /** 最后提交审核时间 */
  val submitTime: OffsetDateTime? = null
)