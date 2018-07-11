package cn.gftaxi.traffic.accident.dto

import java.time.OffsetDateTime

/**
 * 修改报案信息用 DTO。
 *
 * @author RJ
 */
data class AccidentDraftDto4Modify(
  val carPlate: String,
  val driverName: String,
  val happenTime: OffsetDateTime,
  val location: String,
  val hitForm: String,
  val hitType: String,
  val describe: String
)
