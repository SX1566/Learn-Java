package cn.gftaxi.traffic.accident.common

/**
 * 案件阶段标记。
 */
enum class CaseStage(private val value: Short) {
  /**
   * 起草中、待上报（案件上报前所处的状态）。
   */
  ToSubmit(1),
  /**
   * 报案处理中（案件上报后设为此状态）。
   */
  Drafting(2),
  /**
   * 登记处理中（首次提交登记信息后设为此状态）。
   */
  Registering(4),
  /**
   * 报告处理中（首次提交报告信息后设为此状态）。
   */
  Reporting(8),
  /**
   * 跟进处理中（报告信息审核通过后设为此状态）。
   */
  Following(16),
  /**
   * 流程处理中（任何相关流程发起后设为此状态）。
   */
  Processing(32),
  /**
   * 已结案（结案流程审批通过后设为此状态）。
   */
  Closed(64);

  fun value(): Short {
    return value
  }
}