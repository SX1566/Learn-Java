package cn.gftaxi.traffic.accident.dto

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
class AccidentDraftDto4Modify : DynamicDto() {
  var carPlate: String? by data
  var driverName: String? by data
  var happenTime: OffsetDateTime? by data
  var location: String? by data
  var hitForm: String? by data
  var hitType: String? by data
  var describe: String? by data

}