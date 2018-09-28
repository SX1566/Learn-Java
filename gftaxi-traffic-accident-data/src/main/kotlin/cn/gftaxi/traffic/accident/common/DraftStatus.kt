package cn.gftaxi.traffic.accident.common

/**
 * 报案状态。
 *
 * @author RJ
 */
enum class DraftStatus(private val value: Short) {
  /**
   * 待上报、草稿。
   */
  ToSubmit(1),
  /**
   * 已上报、待登记（报案处理中）。
   */
  Drafting(2),
  /**
   * 已登记（报案阶段已完成）。
   */
  Drafted(4);

  fun value(): Short {
    return value
  }
}
