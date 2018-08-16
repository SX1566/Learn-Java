package cn.gftaxi.traffic.accident.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 更新事故其他物体信息用 DTO。
 *
 * @author RJ
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccidentOtherDto4Update(
  @JsonIgnore
  var changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) {
  var id: Int? by changedProperties
  var sn: Short? by changedProperties
  var name: String? by changedProperties
  var type: String? by changedProperties
  var belong: String? by changedProperties
  var linkmanName: String? by changedProperties
  var linkmanPhone: String? by changedProperties
  var damageState: String? by changedProperties
  var damageMoney: BigDecimal? by changedProperties
  var actualMoney: BigDecimal? by changedProperties
  var followType: String? by changedProperties
  var updatedTime: OffsetDateTime? by changedProperties

  override fun toString(): String {
    return "${AccidentOtherDto4Update::class.simpleName}=$changedProperties"
  }
}