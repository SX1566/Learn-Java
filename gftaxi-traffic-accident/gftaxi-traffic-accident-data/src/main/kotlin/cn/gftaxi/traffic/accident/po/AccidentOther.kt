package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.dto.AccidentOtherDto4Form
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfoWithParent
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 事故其他物体 PO。
 *
 * @author RJ
 */
@Entity
@Table(name = "gf_accident_other")
class AccidentOther : AccidentSubListBaseInfoWithParent() {
  /** 归属 */
  @get:Column(length = 50)
  var belong: String? by holder
  /** 联系人 */
  @get:Column(length = 50)
  var linkmanName: String? by holder
  /** 联系电话 */
  @get:Column(length = 50)
  var linkmanPhone: String? by holder
  /** 受损情况 */
  @get:Column(length = 50)
  var damageState: String? by holder
  /** 损失预估（元） */
  @get:Column(precision = 10, scale = 2)
  var guessMoney: BigDecimal? by holder
  /** 实际损失（元） */
  @get:Column(precision = 10, scale = 2)
  var actualMoney: BigDecimal? by holder

  companion object {
    /** [AccidentOtherDto4Form] 转化为 [AccidentOther] */
    fun from(dto: AccidentOtherDto4Form) :AccidentOther{
      return AccidentOther().apply{
        copy(dto, this)
        dto.belong?.let { this.belong = it }
        dto.linkmanName?.let { this.linkmanName = it }
        dto.linkmanPhone?.let { this.linkmanPhone = it }
        dto.damageState?.let { this.damageState = it }
        dto.guessMoney?.let { this.guessMoney = it }
        dto.actualMoney?.let { this.actualMoney = it }
      }
    }
  }
}