package cn.gftaxi.traffic.accident.dto

import tech.simter.category.po.Category.Status
import tech.simter.category.po.converter.CategoryStatusConverter
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
  /** 二级分类排序号 */
  @Id val sn: String,
  /** 所属一级分类编码 */
  val belong: String,
  /** 名称 */
  val name: String,
  /** 状态 */
  @Convert(converter = CategoryStatusConverter::class)
  val status: Status
)