package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.common.Sex
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfoWithParent
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 事故当事人 PO。
 *
 * @author RJ
 */
@Entity
@Table(name = "gf_accident_people")
class AccidentPeople : AccidentSubListBaseInfoWithParent() {
  /** 性别 */
  var sex: Sex? by holder
  /** 联系电话 */
  @get:Column(length = 50)
  var phone: String? by holder
  /** 交通方式 */
  @get:Column(length = 50)
  var transportType: String? by holder
  /** 事故责任 */
  @get:Column(length = 50)
  var duty: String? by holder
  /** 伤亡情况 */
  @get:Column(length = 50)
  var damageState: String? by holder
  /** 预估医疗费（元） */
  @get:Column(precision = 10, scale = 2)
  var guessTreatmentMoney: BigDecimal? by holder
  /** 预估赔偿损失（元） */
  @get:Column(precision = 10, scale = 2)
  var guessCompensateMoney: BigDecimal? by holder
  /** 实际医疗费（元） */
  @get:Column(precision = 10, scale = 2)
  var actualTreatmentMoney: BigDecimal? by holder
  /** 实际赔偿损失（元） */
  @get:Column(precision = 10, scale = 2)
  var actualCompensateMoney: BigDecimal? by holder
}