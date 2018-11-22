package cn.gftaxi.traffic.accident.common

import cn.gftaxi.traffic.accident.common.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.common.Utils.SUB_LIST_PROPERTY_KEYS
import cn.gftaxi.traffic.accident.common.Utils.subListToFields
import cn.gftaxi.traffic.accident.dto.AccidentCarDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentOtherDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentPeopleDto4Form
import cn.gftaxi.traffic.accident.po.AccidentCar
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentOther
import cn.gftaxi.traffic.accident.po.AccidentPeople
import cn.gftaxi.traffic.accident.po.base.AccidentSubListBaseInfo
import org.springframework.data.domain.Page
import tech.simter.operation.po.Field
import tech.simter.operation.po.Operator
import tech.simter.reactive.context.SystemContext.User
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.temporal.ChronoUnit
import javax.json.Json

/** 转换 [Page] 为平台特定的 [Map] 结构 */
fun <T : Any> Page<T>.convert(): Map<String, Any?> = mapOf(
  "count" to this.totalElements,
  "pageNo" to this.pageable.pageNumber + 1,
  "pageSize" to this.pageable.pageSize,
  "rows" to this.content
)

/** 转换 [User] 为 [Operator]  */
fun User.toOperator(): Operator = this.let { Operator(id = it.id.toString(), name = it.name) }

/**
 * 将 [AccidentCase] 转换为 [Field] 的 [List]
 * @param[data] 更新字段的 [Map]
 */
@Suppress("UNCHECKED_CAST")
fun AccidentCase.toFields(data: Map<String, Any?>): List<Field>? {
  val properties = AccidentCase.propertieKeys.filter { !SUB_LIST_PROPERTY_KEYS.contains(it) }
  val comments = AccidentCase.comments
  val propertieTypes = AccidentCase.propertieTypes
  // 1. 基本属性的更新记录
  val fields = data.filter { properties.contains(it.key) }.map {
    val key = it.key
    val newValue = it.value
    val oldValue = this.data[key]
    val type = propertieTypes[key]!!
    val (oldValueString, newValueString) = when (type) {
      LocalDate::class -> Pair((oldValue as LocalDate?)?.format(ISO_LOCAL_DATE),
        (newValue as LocalDate?)?.format(ISO_LOCAL_DATE))
      OffsetDateTime::class -> Pair((oldValue as OffsetDateTime?)?.format(FORMAT_DATE_TIME_TO_MINUTE),
        (newValue as OffsetDateTime?)?.format(FORMAT_DATE_TIME_TO_MINUTE))
      else ->
        Pair(oldValue?.toString(), newValue?.toString())
    }
    Field(id = key, name = comments[key] ?: "",
      type = type.simpleName.toString(),
      oldValue = oldValueString,
      newValue = newValueString
    )
  } +
    // 2. 如果当事车辆被更新时记录被更新的行
    (if (data.containsKey("cars")) {
      subListToFields(
        pos = this.cars,
        dtos = data["cars"] as List<AccidentCarDto4Form>?,
        type = AccidentCar::class.simpleName!!,
        idPrefix = "cars",
        dtoFields = AccidentCarDto4Form.propertieNames
      )
    } else listOf()) +
    // 3. 如果当事人被更新时记录被更新的行
    (if (data.containsKey("peoples")) {
      subListToFields(
        pos = this.peoples,
        dtos = data["peoples"] as List<AccidentPeopleDto4Form>?,
        type = AccidentPeople::class.simpleName!!,
        idPrefix = "peoples",
        dtoFields = AccidentPeopleDto4Form.propertieNames
      )
    } else listOf()) +
    // 4. 如果其他物品被更新时记录被更新的行
    (if (data.containsKey("others")) {
      subListToFields(
        pos = this.others,
        dtos = data["others"] as List<AccidentOtherDto4Form>?,
        type = AccidentOther::class.simpleName!!,
        idPrefix = "others",
        dtoFields = AccidentOtherDto4Form.propertieNames
      )
    } else listOf())
  return if (fields.isEmpty()) null else fields
}

/**
 * 将 [AccidentSubListBaseInfo] 被赋值的字段进行序列化
 * @param[filter] 对被赋值的字段进行筛选的函数
 */
fun AccidentSubListBaseInfo.toJson(filter: ((Map.Entry<String, Any?>) -> Boolean)? = null): String {
  val builder = Json.createObjectBuilder()
  (filter?.let { this.data.filter(it) } ?: this.data)
    .forEach { key, value ->
      if (key == "id") return@forEach
      value?.let {
        when (it) {
          is Short -> builder.add(key, it.toInt())
          is BigDecimal -> builder.add(key, it)
          is Int -> builder.add(key, it)
          is OffsetDateTime -> builder.add(key, it.format(FORMAT_DATE_TIME_TO_MINUTE))
          else -> builder.add(key, it.toString())
        }
      } ?: builder.addNull(key)
    }
  return builder.build().toString()
}

/**
 * 事故工具类。
 *
 * @author RJ
 * @author zh
 */
object Utils {
  /** 格式化日期为 yyyy-MM-dd HH:mm 格式的处理器 */
  val FORMAT_DATE_TIME_TO_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  /** 格式化日期为 yyyyMMdd 格式的处理器 */
  val FORMAT_TO_YYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  /** 格式化年月为 yyyyMM 格式的处理器 */
  val FORMAT_TO_YYYYMM: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM")
  /** 事故信息中为子列表的属性名 */
  val SUB_LIST_PROPERTY_KEYS = listOf("cars", "peoples", "others")

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

  /** 计算逾期的天数和小时数，不足一小时按一小时计算
   * @return 格式如: 1d8h (表示相差1天8小时)，未逾期返回空字符串
   */
  fun calculateOverdueDayAndHour(start: OffsetDateTime, end: OffsetDateTime, overdueSeconds: Long): String {
    val seconds = Duration.between(start, end).get(ChronoUnit.SECONDS) - overdueSeconds
    if (seconds <= 0) return ""
    val proportion = 60 * 60
    val hour = seconds / proportion + if (seconds % proportion > 0) 1 else 0
    return (if (hour / 24 != 0L) "${hour / 24}d" else "") +
      (if (hour % 24 != 0L) "${hour % 24}h" else "")
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

  /**
   * 将 [AccidentSubListBaseInfo] 的 [List] 转换成 [Field] 的 [List]
   * @param[pos] 修改前的 po 的 [List]
   * @param[dtos] 被修改部分的 dto 的 [List]
   * @param[type]  [Field.type] 的值
   * @param[idPrefix]  [Field.id] 的值的前缀
   * @param[dtoFields] 可修改的字段 即 [dto] 的字段
   */
  fun subListToFields(pos: List<AccidentSubListBaseInfo>?, dtos: List<AccidentSubListBaseInfo>?, type: String,
                      idPrefix: String, dtoFields: List<String>): List<Field> {
    return (dtos?.filter { it.data.size > 1 }?.map { dto ->
      val po = pos?.find { it.id == dto.id }
      Field(id = "$idPrefix.${dto.id}",
        name = po?.let { "修改${(dto.type ?: it.type) + (dto.name ?: it.name)}" } ?: "新增${dto.type + dto.name}",
        type = type,
        oldValue = po?.toJson { dto.data.containsKey(it.key) },
        newValue = dto.toJson()
      )
    } ?: listOf()) +
      (pos?.filter { dtos == null || !dtos.map { it.id }.contains(it.id) }?.map { po ->
        Field(id = "$idPrefix.${po.id}",
          name = "删除${po.type + po.name}",
          type = type,
          oldValue = po.toJson { dtoFields.contains(it.key) },
          newValue = null
        )
      } ?: listOf())
  }
}