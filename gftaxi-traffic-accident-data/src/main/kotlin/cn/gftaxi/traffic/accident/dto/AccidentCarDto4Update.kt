package cn.gftaxi.traffic.accident.dto

import java.math.BigDecimal

/**
 * 更新事故当事车辆信息用 DTO。
 *
 * @author RJ
 */
class AccidentCarDto4Update : AccidentRegisterSubListBaseDto() {
  var model: String? by data
  var towCount: Short? by data
  var towMoney: BigDecimal? by data
  var repairType: String? by data
  var repairMoney: BigDecimal? by data
  var damageState: String? by data
  var damageMoney: BigDecimal? by data
}