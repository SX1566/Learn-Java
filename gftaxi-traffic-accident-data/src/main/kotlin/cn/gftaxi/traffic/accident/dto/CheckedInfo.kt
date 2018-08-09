package cn.gftaxi.traffic.accident.dto

/**
 * 审核信息 DTO。
 *
 * @author RJ
 */
data class CheckedInfo constructor(
  /** 审核结果：true-通过、false-不通过 */
  val passed: Boolean,
  /** 审核意见 */
  val comment: String? = null,
  /** 附件 ID */
  val attachmentId: String? = null,
  /** 附件 名称*/
  val attachmentName: String? = null
)