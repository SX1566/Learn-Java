package cn.gftaxi.traffic.accident.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime

/**
 * 修改报案信息用动态 DTO。
 *
 * 特别注意类中的属性需要定义为非只读属性 `var` 而不是 `val`，并且全部属性需要定义为 nullable，且不要设置任何默认值。
 *
 * @author RJ
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccidentDraftDto4Modify(
  @JsonIgnore
  var changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) {
  var carPlate: String? by changedProperties
  var driverName: String? by changedProperties
  var happenTime: OffsetDateTime? by changedProperties
  var location: String? by changedProperties
  var hitForm: String? by changedProperties
  var hitType: String? by changedProperties
  var describe: String? by changedProperties

  override fun toString(): String {
    return "${AccidentDraftDto4Modify::class.simpleName}=$changedProperties"
  }

  constructor(carPlate: String, driverName: String, happenTime: OffsetDateTime,
              location: String, hitForm: String, hitType: String, describe: String)
    : this(mutableMapOf<String, Any?>(
    "carPlate" to carPlate,
    "driverName" to driverName,
    "happenTime" to happenTime,
    "location" to location,
    "hitForm" to hitForm,
    "hitType" to hitType,
    "describe" to describe
  ).withDefault { null })
}