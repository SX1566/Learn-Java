package cn.gftaxi.traffic.accident.dto

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故报告汇总统计 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentReportDto4StatSummary constructor(
  /** 范围，按月统计时格式为"yyyy年MM月"，按季度统计时格式为"yyyy年第n季度"，按年统计时时格式为"yyyy年" */
  @Id val scope: String,
  /** 事故总数 */
  val total: Int,
  /** 出险结案案件数 */
  val closed: Int,
  /** 在案已审案件数，仅含审核通过 */
  val checked: Int,
  /** 在案在审案件数，包包含审核不通过 */
  val checking: Int,
  /** 尚待报告案件数 */
  val reporting: Int,
  /** 逾期报告案件数 */
  val overdueReport: Int
) : Serializable