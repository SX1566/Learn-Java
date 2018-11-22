package cn.gftaxi.traffic.accident.common

import tech.simter.operation.OperationType.*

/**
 * 事故操作常数。
 *
 * @author zh
 */
object AccidentOperation {
  /** 事故操作类型的通用分组标记 */
  const val ACCIDENT_OPERATION_CLUSTER: String = "accident"

  // 被操作者的类型
  /** 事故报案信息的被操作类型 */
  const val ACCIDENT_DRAFT_TARGET_TYPE: String = "AccidentDraft"
  /** 事故登记信息的被操作类型 */
  const val ACCIDENT_REGISTER_TARGET_TYPE: String = "AccidentRegister"
  /** 事故报告信息的被操作类型 */
  const val ACCIDENT_REPORT_TARGET_TYPE: String = "AccidentReport"

  // 操作结果
  /** 审核通过的操作结果 */
  const val APPROVAL_RESUTLT = "审核通过"
  /** 审核不通过的操作结果 */
  const val REJECTION_RESUTLT = "审核不通过"
  /** 提交不逾期的操作结果 */
  const val CONFIRMATION_NOT_OVERDUE_RESUTLT = "提交未逾期"
  /** 提交逾期的操作结果 */
  const val CONFIRMATION_OVERDUE_RESUTLT = "提交逾期了"
  /** 上报案件不逾期的操作结果 */
  const val CREATION_NOT_OVERDUE_RESUTLT = "上报未逾期"
  /** 上报案件逾期的操作结果 */
  const val CREATION_OVERDUE_RESUTLT = "上报逾期了"

  // 操作标题
  val operationTitles = mapOf(
    Creation.name + ACCIDENT_DRAFT_TARGET_TYPE to "上报案件",
    Modification.name + ACCIDENT_DRAFT_TARGET_TYPE to "修改事故报案",
    Modification.name + ACCIDENT_REGISTER_TARGET_TYPE to "修改事故登记",
    Confirmation.name + ACCIDENT_REGISTER_TARGET_TYPE to "提交事故登记",
    Rejection.name + ACCIDENT_REGISTER_TARGET_TYPE to "审核事故登记",
    Approval.name + ACCIDENT_REGISTER_TARGET_TYPE to "审核事故登记",
    Modification.name + ACCIDENT_REPORT_TARGET_TYPE to "修改事故报告",
    Confirmation.name + ACCIDENT_REPORT_TARGET_TYPE to "提交事故报告",
    Rejection.name + ACCIDENT_REPORT_TARGET_TYPE to "审核事故报告",
    Approval.name + ACCIDENT_REPORT_TARGET_TYPE to "审核事故报告"
  )
}