package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentReport
import cn.gftaxi.traffic.accident.po.converter.AccidentReportStatusConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity

/**
 * 事故报告信息表单用 DTO。
 *
 * @author RJ
 */
@Entity
class AccidentReportDto4Form : AccidentCaseDto4FormBaseInfo() {
  @get:Convert(converter = AccidentReportStatusConverter::class)
  var status: AccidentReport.Status? by data
  /** 报案时间 */
  var draftTime: OffsetDateTime? by data
  /** 是否逾期报案 */
  var overdueDraft: Boolean? by data
  /** 登记时间 */
  var registerTime: OffsetDateTime? by data
  /** 是否逾期登记 */
  var overdueRegister: Boolean? by data
  /** 报告时间 */
  var reportTime: OffsetDateTime? by data
  /** 是否逾期报告 */
  var overdueReport: Boolean? by data
  /** 诉讼信息 */
  var lawsuit: String? by data

  // 历史统计
  var historyAccidentCount: Short? by data
  var historyTrafficOffenceCount: Short? by data
  var historyServiceOffenceCount: Short? by data
  var historyComplainCount: Short? by data
}