package cn.gftaxi.traffic.accident.common

/**
 * 司机驾驶状态。
 *
 * @author RJ
 */
enum class DriverType(private val value: Short) {
  /**
   * 正班。
   */
  Official(1),
  /**
   * 替班。
   */
  Shift(2),
  /**
   * 非编。
   */
  Outside(4);

  fun value(): Short {
    return value
  }
}