package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.converter.AccidentRegisterStatusConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity

/**
 * 事故登记信息表单用 DTO。
 *
 * @author JF
 * @author RJ
 */
@Entity
class AccidentRegisterDto4Form : AccidentCaseDto4FormBaseInfo() {
  @get:Convert(converter = AccidentRegisterStatusConverter::class)
  var status: AccidentRegister.Status? by data
  /** 报案时间 */
  var draftTime: OffsetDateTime? by data
  /** 是否逾期报案 */
  var overdueDraft: Boolean? by data
  /** 登记时间 */
  var registerTime: OffsetDateTime? by data
  /** 是否逾期登记 */
  var overdueRegister: Boolean? by data

  // 历史统计
  var historyAccidentCount: Short? by data
  var historyTrafficOffenceCount: Short? by data
  var historyServiceOffenceCount: Short? by data
  var historyComplainCount: Short? by data
}