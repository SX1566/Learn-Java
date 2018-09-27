package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfoWithParent
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 事故当事车辆 PO。
 *
 * @author RJ
 */
@Entity
@Table(name = "gf_accident_car")
class AccidentCar : AccidentSubListBaseInfoWithParent() {
  /** 车型：出租车、小轿车、... */
  @get:Column(length = 50)
  var model: String? by holder
  /** 拖车次数 */
  var towCount: Short? by holder
  /** 维修分类：厂修、外修 */
  @get:Column(length = 50)
  var repairType: String? by holder
  /** 受损情况 */
  @get:Column(length = 50)
  var damageState: String? by holder
  /** 预估拖车费（元） */
  @get:Column(precision = 10, scale = 2)
  var guessTowMoney: BigDecimal? by holder
  /** 预估维修费（元） */
  @get:Column(precision = 10, scale = 2)
  var guessRepairMoney: BigDecimal? by holder
  /** 实际拖车费（元） */
  @get:Column(precision = 10, scale = 2)
  var actualTowMoney: BigDecimal? by holder
  /** 实际维修费（元） */
  @get:Column(precision = 10, scale = 2)
  var actualRepairMoney: BigDecimal? by holder
}
