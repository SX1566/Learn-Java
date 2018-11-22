package cn.gftaxi.traffic.accident.po.base

import cn.gftaxi.traffic.accident.po.AccidentCase
import javax.persistence.FetchType.LAZY
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass

/**
 * 事故关联对象的公共信息积累，如当事车辆、当事人、其他物体等的公共信息。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentSubListBaseInfoWithParent : AccidentSubListBaseInfo() {
  /** 所属案件 */
  @get:ManyToOne(optional = false, fetch = LAZY)
  @get:JoinColumn(name = "pid", nullable = false)
  var parent: AccidentCase? by holder
}