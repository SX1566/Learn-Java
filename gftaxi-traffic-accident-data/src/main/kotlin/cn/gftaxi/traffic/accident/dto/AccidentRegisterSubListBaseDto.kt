package cn.gftaxi.traffic.accident.dto

import java.time.OffsetDateTime

/**
 * 更新事故当事车辆、当事人、其他物体信息用 DTO 的公共属性。
 *
 * @author RJ
 */
open class AccidentRegisterSubListBaseDto : DynamicDto() {
  var id: Int? by data
  var sn: Short? by data
  var name: String? by data
  var type: String? by data
  var followType: String? by data
  var updatedTime: OffsetDateTime? = null // disabled update
}