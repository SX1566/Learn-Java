package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentPeople
import java.math.BigDecimal
import javax.persistence.Entity

/**
 * 更新事故当事人信息用 DTO。
 *
 * @author RJ
 */
@Entity
class AccidentPeopleDto4Form : AccidentCaseSubListDto4FormBaseInfo() {
  var sex: AccidentPeople.Sex? by data
  var phone: String? by data
  var transportType: String? by data
  var duty: String? by data
  var damageState: String? by data
  var damageMoney: BigDecimal? by data
  var treatmentMoney: BigDecimal? by data
  var compensateMoney: BigDecimal? by data
}