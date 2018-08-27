package cn.gftaxi.traffic.accident.dto

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * 动态 DTO 基类。
 *
 * 特别注意子类中的属性需要定义为非只读属性 `var` 而不是 `val`，并且全部属性需要定义为可空，且不要设置任何默认值。
 *
 * @author RJ
 */
open class DynamicDto {
  /** 属性数据持有者 */
  @JsonIgnore
  val data: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }

  override fun toString(): String {
    return "${javaClass.simpleName}=$data"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DynamicDto

    if (data != other.data) return false

    return true
  }

  override fun hashCode(): Int {
    return data.hashCode()
  }
}