package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentOther
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfo
import java.math.BigDecimal
import javax.persistence.Entity
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * 事故其他物体信息 DTO。
 *
 * @author RJ
 * @author zh
 */
@Entity
class AccidentOtherDto4Form : AccidentSubListBaseInfo() {
  var belong: String? by holder
  var linkmanName: String? by holder
  var linkmanPhone: String? by holder
  var damageState: String? by holder
  var guessMoney: BigDecimal? by holder
  var actualMoney: BigDecimal? by holder

  companion object {
    /** 转换 [AccidentOther] 为 [AccidentOtherDto4Form] */
    fun from(po: AccidentOther): AccidentOtherDto4Form {
      return AccidentOtherDto4Form().apply {
        copy(po, this)
        po.belong?.let { this.belong = po.belong }
        po.linkmanName?.let { this.linkmanName = po.linkmanName }
        po.linkmanPhone?.let { this.linkmanPhone = po.linkmanPhone }
        po.damageState?.let { this.damageState = po.damageState }
        po.guessMoney?.let { this.guessMoney = po.guessMoney }
        po.actualMoney?.let { this.actualMoney = po.actualMoney }
      }
    }

    val propertieNames = AccidentOtherDto4Form::class.memberProperties
      .filterIsInstance<KMutableProperty<*>>()
      .map { it.name }
  }
}