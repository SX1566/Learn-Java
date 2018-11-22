package cn.gftaxi.traffic.accident.common

/**
 * 审查状态。
 *
 * 用于标记事故登记、事故报告的处理过程的状态。
 *
 * @author RJ
 */
enum class AuditStatus(private val value: Short) {
  /**
   * 待提交、草稿。
   */
  ToSubmit(1),
  /**
   * 待审核。
   */
  ToCheck(2),
  /**
   * 审核不通过。
   */
  Rejected(4),
  /**
   * 审核通过。
   */
  Approved(8);

  fun value(): Short {
    return value
  }
}
