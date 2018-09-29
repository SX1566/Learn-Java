package cn.gftaxi.traffic.accident.common

import org.springframework.data.domain.Page
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** 转换 [Page] 为平台特定的 [Map] 结构 */
fun <T : Any> Page<T>.convert(): Map<String, Any?> = mapOf(
  "count" to this.totalElements,
  "pageNo" to this.pageable.pageNumber + 1,
  "pageSize" to this.pageable.pageSize,
  "rows" to this.content
)

/**
 * 事故工具类。
 *
 * @author RJ
 */
object Utils {
  /** 格式化日期为 yyyy-MM-dd HH:mm 格式的处理器 */
  val FORMAT_DATE_TIME_TO_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  /** 格式化日期为 yyyyMMdd 格式的处理器 */
  val FORMAT_TO_YYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  /** 格式化年月为 yyyyMM 格式的处理器 */
  val FORMAT_TO_YYYYMM: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM")
  /** 事故报案信息的操作类型 */
  const val ACCIDENT_DRAFT_TARGET_TYPE: String = "AccidentDraft"
  /** 事故登记信息的操作类型 */
  const val ACCIDENT_REGISTER_TARGET_TYPE: String = "AccidentRegister"
  /** 事故报告信息的操作类型 */
  const val ACCIDENT_REPORT_TARGET_TYPE: String = "AccidentReport"

  /**
   * 将车号改造为 "粤A123456" 格式。
   *
   * 1. "粤A.123456"  to "粤A123456"
   * 2. "粤A•123456"  to "粤A123456"
   * 3. "粤A・123456" to "粤A123456"
   * 4. "Q2M45"      to "Q2M45"
   */
  fun polishCarPlate(carPlate: String): String {
    return carPlate.replace("•", "").replace("・", "").replace(".", "")
  }

  /** 判断是否逾期 */
  fun isOverdue(from: OffsetDateTime, to: OffsetDateTime, overdueSeconds: Long): Boolean {
    return Duration.between(from, to).get(ChronoUnit.SECONDS) > overdueSeconds
  }

  /** 计算两个时间之间的年份数，不足一年时舍弃 */
  fun calculateYears(start: LocalDate, end: LocalDate): Int {
    return if (end <= start) 0
    else {
      if (end.withYear(start.year) >= start) end.year - start.year
      else end.year - start.year - 1
    }
  }

  /** 计算两个日期之间的年份数，不足一年时舍弃 */
  fun calculateYears(start: LocalDate, end: OffsetDateTime): Int {
    return calculateYears(start, end.toLocalDate())
  }

  /** 合并求和 */
  fun sum(vararg decimals: BigDecimal?): BigDecimal? {
    return if (decimals.isEmpty()) null
    else decimals.reduce { accumulator, otherItem ->
      when {
        accumulator == null -> otherItem
        otherItem == null -> null
        else -> accumulator.add(otherItem)
      }
    }
  }
}