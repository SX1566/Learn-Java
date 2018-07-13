package cn.gftaxi.traffic.accident.dto

import tech.simter.category.po.Category.Status
import tech.simter.category.po.converter.CategoryStatusConverter
import java.io.Serializable
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 交通事故二级分类 DTO。
 *
 * @author JF
 */
@Entity
data class SecondaryCategoryDto constructor(
  /** 所属一级分类编码 */
  @Id val belong: String,
  /** 排序号 */
  @Id val sn: String,
  /** 名称 */
  val name: String,
  /** 状态 */
  @Convert(converter = CategoryStatusConverter::class)
  val status: Status
) : Serializable