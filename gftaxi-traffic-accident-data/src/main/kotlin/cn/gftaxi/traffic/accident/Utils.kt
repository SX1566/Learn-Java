package cn.gftaxi.traffic.accident

import java.time.format.DateTimeFormatter

/**
 * 事故工具类。
 *
 * @author RJ
 */
object Utils {
  /** 格式化日期为 yyyy-MM-dd HH:mm 格式的处理器 */
  val FORMAT_DATE_TIME_TO_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  /** 格式化日期为 yyyyMMdd 格式的处理器 */
  val FORMAT_TO_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd")
}