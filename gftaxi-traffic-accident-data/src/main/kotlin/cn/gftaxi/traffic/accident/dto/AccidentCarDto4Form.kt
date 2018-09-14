package cn.gftaxi.traffic.accident.dto

import java.math.BigDecimal
import javax.persistence.Entity

/**
 * 更新事故当事车辆信息用 DTO。
 *
 * @author RJ
 */
@Entity
class AccidentCarDto4Form : AccidentCaseSubListDto4FormBaseInfo() {
  var model: String? by data
  var towCount: Short? by data
  var repairType: String? by data
  var damageState: String? by data
  var guessTowMoney: BigDecimal? by data
  var guessRepairMoney: BigDecimal? by data
  var actualTowMoney: BigDecimal? by data
  var actualRepairMoney: BigDecimal? by data
}