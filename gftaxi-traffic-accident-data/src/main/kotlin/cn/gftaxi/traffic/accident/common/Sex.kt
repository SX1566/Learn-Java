package cn.gftaxi.traffic.accident.common

/**
 * 性别。
 *
 * @author RJ
 */
enum class Sex(private val value: Short) {
  /**
   * 未设置。
   */
  NotSet(0),
  /**
   * 男。
   */
  Male(1),
  /**
   * 女。
   */
  Female(2);

  fun value(): Short {
    return value
  }
}
