package cn.gftaxi.webflux.dynamicdto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * 动态 DTO 例子。
 *
 * 这个类使用了 Kotlin 的 [Delegated Properties](https://kotlinlang.org/docs/reference/delegated-properties.html) 中的
 * [Storing Properties in a Map](https://kotlinlang.org/docs/reference/delegated-properties.html#storing-properties-in-a-map) 技术
 * 将有设置过值的属性以键值对的形式保存在 [changedProperties] 属性中。
 * 这个主意来源于 StackOverflow [How to do PATCH properly in strongly typed languages based on Spring](https://stackoverflow.com/questions/36907723#answer-37010895)。
 *
 * 特别注意类中的属性需要定义为非只读属性 `var` 而不是 `val`，并且全部属性需要定义为 nullable，且不要设置任何默认值。
 *
 * @author RJ
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DynamicDto(
  @JsonIgnore
  var changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) {
  var name: String? by changedProperties
  var code: String? by changedProperties
  var int: Int? by changedProperties
  var double: Double? by changedProperties
  var bigDecimal: BigDecimal? by changedProperties
  @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd HH:00")
  var offsetDateTime: OffsetDateTime?  by changedProperties

  override fun toString(): String {
    return "AccidentRegisterDto4Update=$changedProperties"
  }
}