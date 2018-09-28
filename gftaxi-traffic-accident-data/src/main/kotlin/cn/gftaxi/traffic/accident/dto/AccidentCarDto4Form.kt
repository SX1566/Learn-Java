package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentCar
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfo
import java.math.BigDecimal
import javax.persistence.Entity

/**
 * 事故当事车辆信息 DTO。
 *
 * @author RJ
 */
@Entity
class AccidentCarDto4Form : AccidentSubListBaseInfo() {
  var model: String? by holder
  var towCount: Short? by holder
  var repairType: String? by holder
  var damageState: String? by holder
  var guessTowMoney: BigDecimal? by holder
  var guessRepairMoney: BigDecimal? by holder
  var actualTowMoney: BigDecimal? by holder
  var actualRepairMoney: BigDecimal? by holder

  companion object {
    /** 转换 [AccidentCar] 为 [AccidentCarDto4Form] */
    fun from(po: AccidentCar): AccidentCarDto4Form {
      return AccidentCarDto4Form().apply {
        copy(po, this)
        po.model?.let { this.model = po.model }
        po.towCount?.let { this.towCount = po.towCount }
        po.repairType?.let { this.repairType = po.repairType }
        po.guessTowMoney?.let { this.guessTowMoney = po.guessTowMoney }
        po.guessRepairMoney?.let { this.guessRepairMoney = po.guessRepairMoney }
        po.actualTowMoney?.let { this.actualTowMoney = po.actualTowMoney }
        po.actualRepairMoney?.let { this.actualRepairMoney = po.actualRepairMoney }
        po.damageState?.let { this.damageState = po.damageState }
      }
    }
  }
}