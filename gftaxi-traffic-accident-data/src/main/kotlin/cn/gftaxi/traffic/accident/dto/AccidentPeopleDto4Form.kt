package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.Sex
import cn.gftaxi.traffic.accident.po.AccidentPeople
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfo
import java.math.BigDecimal
import javax.persistence.Entity

/**
 * 事故当事人信息 DTO。
 *
 * @author RJ
 */
@Entity
class AccidentPeopleDto4Form : AccidentSubListBaseInfo() {
  var sex: Sex? by holder
  var phone: String? by holder
  var transportType: String? by holder
  var duty: String? by holder
  var damageState: String? by holder
  var guessTreatmentMoney: BigDecimal? by holder
  var guessCompensateMoney: BigDecimal? by holder
  var actualTreatmentMoney: BigDecimal? by holder
  var actualCompensateMoney: BigDecimal? by holder

  companion object {
    /** 转换 [AccidentPeople] 为 [AccidentPeopleDto4Form] */
    fun from(po: AccidentPeople): AccidentPeopleDto4Form {
      return AccidentPeopleDto4Form().apply {
        copy(po, this)
        po.sex?.let { this.sex = po.sex }
        po.phone?.let { this.phone = po.phone }
        po.transportType?.let { this.transportType = po.transportType }
        po.duty?.let { this.duty = po.duty }
        po.damageState?.let { this.damageState = po.damageState }
        po.guessTreatmentMoney?.let { this.guessTreatmentMoney = po.guessTreatmentMoney }
        po.guessCompensateMoney?.let { this.guessCompensateMoney = po.guessCompensateMoney }
        po.actualTreatmentMoney?.let { this.actualTreatmentMoney = po.actualTreatmentMoney }
        po.actualCompensateMoney?.let { this.actualCompensateMoney = po.actualCompensateMoney }
      }
    }
  }
}