package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.IdEntity
import java.time.OffsetDateTime
import javax.persistence.MappedSuperclass

/**
 * 事故关联对象的公共信息 DTO，如当事车辆、当事人、其他物体等 DTO 的公共信息。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentCaseSubListDto4FormBaseInfo : IdEntity, DynamicDto() {
  @get:javax.persistence.Id
  @get:org.springframework.data.annotation.Id
  override var id: Int? by data
  var sn: Short? by data
  var name: String? by data
  var type: String? by data
  var followType: String? by data
  var updatedTime: OffsetDateTime? = null // disabled update
}