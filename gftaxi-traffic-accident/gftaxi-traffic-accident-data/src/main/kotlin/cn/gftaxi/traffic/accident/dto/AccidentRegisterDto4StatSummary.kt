package cn.gftaxi.traffic.accident.dto

import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记汇总统计 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4StatSummary constructor(
  /** 统计范围，按月统计时格式为"yyyyMM"，按年时格式为"yyyy"，按季度时格式为"yyyyQn"*/
  @Id val scope: String,
  /** 事故报案总数 */
  val total: Int,
  /** 已登已审案件数，只包含审核通过的案件 */
  val checked: Int,
  /** 已登在审案件数，包含审核不通过的案件 */
  val checking: Int,
  /** 尚未登记案件数 */
  val drafting: Int,
  /** 逾期报案案件数 */
  val overdueDraft: Int,
  /** 逾期登记案件数 */
  val overdueRegister: Int
)