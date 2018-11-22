package cn.gftaxi.traffic.accident.po.base

import cn.gftaxi.traffic.accident.common.DynamicBean
import cn.gftaxi.traffic.accident.common.IdEntity
import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.MappedSuperclass

/**
 * 事故关联对象的公共信息积累，如当事车辆、当事人、其他物体等的公共信息。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentSubListBaseInfo : IdEntity, DynamicBean() {
  @get:javax.persistence.Id
  @get:org.springframework.data.annotation.Id
  @get:GeneratedValue(strategy = IDENTITY)
  override var id: Int? by holder
  /** 同一事故内的序号 */
  @get:Column(nullable = false)
  var sn: Short? by holder
  /** 车号、姓名、物体名称等 */
  @get:Column(length = 50, nullable = false)
  var name: String? by holder
  /** 分类 */
  @get:Column(length = 50, nullable = false)
  var type: String? by holder
  /** 跟进形式 */
  @get:Column(length = 50)
  var followType: String? by holder
  /** 更新时间 */
  @get:Column(nullable = false)
  var updatedTime: OffsetDateTime? by holder

  companion object {
    /** 复制 */
    fun copy(source: AccidentSubListBaseInfo, target: AccidentSubListBaseInfo) {
      target.apply {
        source.id?.let { this.id = it }
        source.sn?.let { this.sn = it }
        source.name?.let { this.name = it }
        source.type?.let { this.type = it }
        source.followType?.let { this.followType = it }
        source.updatedTime?.let { this.updatedTime = it }
      }
    }
  }
}