package cn.gftaxi.traffic.accident.dto

import java.time.OffsetDateTime

/**
 * 上报案件信息用 DTO。
 *
 * @author RJ
 */
data class AccidentDraftDto4Submit(
  val carPlate: String,
  val driverName: String,
  val happenTime: OffsetDateTime,
  val location: String,
  val hitForm: String,
  val hitType: String,
  val describe: String?,
  val source: String,
  val authorName: String,
  val authorId: String,
  val reportTime: OffsetDateTime = OffsetDateTime.now()
)
