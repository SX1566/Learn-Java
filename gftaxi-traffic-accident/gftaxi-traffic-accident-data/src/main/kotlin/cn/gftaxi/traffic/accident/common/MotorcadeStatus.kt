package cn.gftaxi.traffic.accident.common

/**
 * 车队状态。
 *
 * @author jw
 */
enum class MotorcadeStatus(private val value: Short) {
  /** 正常 */
  Enabled(0),
  /** 禁用 */
  Disabled(1),
  /** 已删除 */
  Deleted(2);

  fun value(): Short {
    return value
  }
}
