package cn.gftaxi.traffic.accident.dto

import java.math.BigDecimal

/**
 * 更新事故其他物体信息用 DTO。
 *
 * @author RJ
 */
class AccidentOtherDto4Update : AccidentRegisterSubListBaseDto() {
  var belong: String? by data
  var linkmanName: String? by data
  var linkmanPhone: String? by data
  var damageState: String? by data
  var damageMoney: BigDecimal? by data
  var actualMoney: BigDecimal? by data
}