package cn.gftaxi.traffic.accident.dto

import java.time.OffsetDateTime
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记的待登记、待审核案件 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4Todo constructor(
  @Id val code: String,
  val carPlate: String,
  val driverName: String,
  val outsideDriver: Boolean,
  val happenTime: OffsetDateTime,
  val hitForm: String,
  val hitType: String,
  val location: String,
  val authorName: String,
  val authorId: String,
  val reportTime: OffsetDateTime,
  val overdueReport: Boolean,
  val registerTime: OffsetDateTime?,
  val overdueRegister: Boolean?,
  /** 提交审核时间 */
  val submitTime: OffsetDateTime?
)