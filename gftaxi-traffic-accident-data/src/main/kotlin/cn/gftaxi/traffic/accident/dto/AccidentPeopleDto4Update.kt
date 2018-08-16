package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentPeople
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 更新事故当事人信息用 DTO。
 *
 * @author RJ
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccidentPeopleDto4Update(
  @JsonIgnore
  var changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) {
  var id: Int? by changedProperties
  var sn: Short? by changedProperties
  var name: String? by changedProperties
  var type: String? by changedProperties
  var sex: AccidentPeople.Sex? by changedProperties
  var phone: String? by changedProperties
  var transportType: String? by changedProperties
  var duty: String? by changedProperties
  var damageState: String? by changedProperties
  var damageMoney: BigDecimal? by changedProperties
  var treatmentMoney: BigDecimal? by changedProperties
  var compensateMoney: BigDecimal? by changedProperties
  var followType: String? by changedProperties
  @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  var updatedTime: OffsetDateTime? by changedProperties

  override fun toString(): String {
    return "${AccidentPeopleDto4Update::class.simpleName}=$changedProperties"
  }
}