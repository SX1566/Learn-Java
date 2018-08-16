package cn.gftaxi.traffic.accident.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 更新事故当事车辆信息用 DTO。
 *
 * @author RJ
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccidentCarDto4Update(
  @JsonIgnore
  var changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) {
  var id: Int? by changedProperties
  var sn: Short? by changedProperties
  var name: String? by changedProperties
  var type: String? by changedProperties
  var model: String? by changedProperties
  var towCount: Short? by changedProperties
  var towMoney: BigDecimal? by changedProperties
  var repairType: String? by changedProperties
  var repairMoney: BigDecimal? by changedProperties
  var damageState: String? by changedProperties
  var damageMoney: BigDecimal? by changedProperties
  var followType: String? by changedProperties
  @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  var updatedTime: OffsetDateTime? by changedProperties

  override fun toString(): String {
    return "${AccidentCarDto4Update::class.simpleName}=$changedProperties"
  }
}