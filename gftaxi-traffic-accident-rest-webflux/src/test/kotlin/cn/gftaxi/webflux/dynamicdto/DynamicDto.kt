package cn.gftaxi.webflux.dynamicdto

import com.fasterxml.jackson.annotation.JsonIgnore
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
class DynamicDto {
  @JsonIgnore
  private val changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
  var name: String? by changedProperties
  var code: String? by changedProperties
  var int: Int? by changedProperties
  var double: Double? by changedProperties
  var bigDecimal: BigDecimal? by changedProperties
  var offsetDateTime: OffsetDateTime?  by changedProperties

  override fun toString(): String {
    return "DynamicDto=$changedProperties"
  }
}