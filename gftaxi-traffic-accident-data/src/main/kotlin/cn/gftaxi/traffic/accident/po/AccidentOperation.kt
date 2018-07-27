package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.converter.AccidentOperationTargetTypeConverter
import cn.gftaxi.traffic.accident.po.converter.AccidentOperationTypeConverter
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故相关事务操作记录 PO。
 *
 * 用于记录：<br>
 * 1. 谁？（[operatorId]、[operatorName]）
 * 2. 什么时候？（[operateTime]）
 * 3. 干了什么事情？（[operationType]、[targetType]、[targetId]）
 * 4. 干得如何？（[tag]、[comment]、[attachmentId]、[attachmentName]）。
 *
 * 如提交记录、审核通过记录、审核不通过记录等。
 *
 * @author RJ
 */
@Entity
@Table(name = "gf_accident_operation")
data class AccidentOperation(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Int? = null,
  /** 操作人ID */
  val operatorId: Int,
  /** 操作人姓名 */
  val operatorName: String,
  /** 操作时间 */
  val operateTime: OffsetDateTime,
  /** 操作类型，如提交、审核等 */
  @Convert(converter = AccidentOperationTypeConverter::class)
  val operationType: OperationType,
  /** 事务类型，如事故登记、事故报告、事故结案等 */
  @Convert(converter = AccidentOperationTargetTypeConverter::class)
  val targetType: TargetType,
  /** 事务ID，如事故登记ID */
  val targetId: Int,
  /**
   * 操作类型的扩展标记。
   *
   * 值由具体的事务自行决定，0 为保留值，代表无特殊意义。
   * 如某事务的审核不通过要分开为小错误和大错误，则可以在相同的 [OperationType.Rejection] 下用此字段不同的值来区分。
   */
  val tag: Int = 0,
  /** 操作结果描述，如可以作为审核意见、审计日志描述等 */
  val comment: String?,
  /** 附件ID */
  val attachmentId: String?,
  /** 附件名称 */
  val attachmentName: String?
) {
  /**
   * 操作类型。
   */
  enum class OperationType(private val value: Short) {
    /**
     * 创建。
     */
    Creation(10),
    /**
     * 修改。
     */
    Modification(20),
    /**
     * 确认提交、提交审核。
     */
    Confirmation(30),
    /**
     * 批准、审核通过。
     */
    Approval(40),
    /**
     * 驳回、审核不通过。
     */
    Rejection(50),
    /**
     * 删除。
     */
    Deletion(90);

    fun value(): Short {
      return value
    }
  }

  /**
   * 事务类型。
   */
  enum class TargetType(private val value: Short) {
    /**
     * 事故报案。
     */
    Draft(10),
    /**
     * 事故登记。
     */
    Register(20),
    /**
     * 事故报告。
     */
    Report(30),
    /**
     * 事故处理。
     */
    Handle(40),
    /**
     * 事故结案。
     */
    Close(50);

    fun value(): Short {
      return value
    }
  }
}