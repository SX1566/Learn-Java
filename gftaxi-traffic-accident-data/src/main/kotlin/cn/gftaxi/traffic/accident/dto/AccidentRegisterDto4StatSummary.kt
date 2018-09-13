package cn.gftaxi.traffic.accident.dto

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记汇总统计 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4StatSummary constructor(
  /** 范围，按月统计时格式为"yyyy年MM月"，按季度或按年时格式为"yyyy年"*/
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
  val overdueCreate: Int,
  /** 逾期登记案件数 */
  val overdueRegister: Int
) : Serializable

/**
 * 统计范围。
 */
enum class ScopeType(private val value: Short) {
  /**
   * 按月。
   */
  Monthly(1),
  /**
   * 按季度。
   */
  Quarterly(2),
  /**
   * 按年。
   */
  Yearly(4);

  fun value(): Short {
    return value
  }
}