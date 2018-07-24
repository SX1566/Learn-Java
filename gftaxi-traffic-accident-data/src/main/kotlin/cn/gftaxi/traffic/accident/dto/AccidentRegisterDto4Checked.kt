package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
import java.time.OffsetDateTime
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记的已审核案件的最后审核信息 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4Checked constructor(
  @Id val code: String,
  val carPlate: String,
  val driverName: String,
  /** 是否非编司机 */
  val outsideDriver: Boolean,
  /** 审核结果：[Approved] 或 [Rejected] */
  val checkedResult: Status,
  /** 审核意见 */
  val checkedComment: String?,
  /** 审核人姓名 */
  val checkerName: String,
  /** 审核次数 */
  val checkedCount: Int,
  /** 审核时间 */
  val checkedTime: OffsetDateTime,
  /** 附件名称 */
  val attachmentName: String?,
  /** 附件 ID */
  val attachmentId: String?
)