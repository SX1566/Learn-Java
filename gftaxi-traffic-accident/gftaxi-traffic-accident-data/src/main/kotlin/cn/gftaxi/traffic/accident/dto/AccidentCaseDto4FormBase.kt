package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.base.AccidentCaseBase
import javax.persistence.MappedSuperclass

/**
 * 案件基础信息 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentCaseDto4FormBase : AccidentCaseBase() {
  // 当事车辆
  @get:javax.persistence.Transient
  @get:org.springframework.data.annotation.Transient
  var cars: List<AccidentCarDto4Form>? by holder

  // 当事人
  @get:javax.persistence.Transient
  @get:org.springframework.data.annotation.Transient
  var peoples: List<AccidentPeopleDto4Form>? by holder

  // 其他物体
  @get:javax.persistence.Transient
  @get:org.springframework.data.annotation.Transient
  var others: List<AccidentOtherDto4Form>? by holder
}