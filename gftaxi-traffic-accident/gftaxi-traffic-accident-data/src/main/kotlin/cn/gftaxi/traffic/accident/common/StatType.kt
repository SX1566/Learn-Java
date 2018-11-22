package cn.gftaxi.traffic.accident.common


/**
 * 统计类型。
 *
 * @author zh
 */
enum class StatType(val pattern: String) {
  /**
   * 按月度统计。
   */
  Monthly("yyyyMM"),
  /**
   * 按年度统计。
   */
  Yearly("yyyy"),
  /**
   * 按季度统计。
   */
  Quarterly("yyyy'Q'Q");
}