package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.DynamicBean
import java.time.OffsetDateTime
import javax.persistence.MappedSuperclass

/**
 * 修改报案信息用 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentDraftDto4FormUpdate : DynamicBean() {
  var carPlate: String? by holder
  var driverName: String? by holder
  var happenTime: OffsetDateTime? by holder
  var location: String? by holder
  var hitForm: String? by holder
  var hitType: String? by holder
  var describe: String? by holder
}