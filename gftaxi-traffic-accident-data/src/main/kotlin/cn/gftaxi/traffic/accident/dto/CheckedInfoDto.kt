package cn.gftaxi.traffic.accident.dto

import tech.simter.operation.po.Attachment

/**
 * 审核信息 DTO。
 *
 * @author RJ
 */
data class CheckedInfoDto constructor(
  /** 审核结果：true-通过、false-不通过 */
  val passed: Boolean,
  /** 审核意见 */
  val comment: String? = null,
  /** 附件 */
  val attachment: Attachment? = null
)