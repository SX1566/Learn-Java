package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.common.*
import cn.gftaxi.traffic.accident.po.converter.AuditStatusConverter
import cn.gftaxi.traffic.accident.po.converter.CaseStageConverter
import cn.gftaxi.traffic.accident.po.converter.DraftStatusConverter
import tech.simter.operation.po.Attachment
import tech.simter.operation.po.converter.AttachmentsConverter
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 案件当前处理情况信息 PO。
 *
 * @author RJ
 */
@Entity
@Table(name = "gf_accident_situation")
class AccidentSituation : IdEntity, DynamicBean() {
  /** 案件 ID */
  @get:Id
  override var id: Int? by holder
  /** 案件主体状态 */
  @get:Convert(converter = CaseStageConverter::class)
  var stage: CaseStage? by holder

  //== 报案相关 ==
  /** 报案来源：BC-BC系统Web端、MAIL-邮件、WEIXIN-微信、SMS-短信、{appId}-应用ID */
  @get:Column(length = 10, nullable = false)
  var source: String? by holder
  /** 报案时间 */
  var draftTime: OffsetDateTime? by holder
  /** 是否逾期报案 */
  var overdueDraft: Boolean? by holder
  /** 接案人姓名 */
  @get:Column(length = 50)
  var authorName: String? by holder
  /** 接案人标识：邮件报案为邮箱、短信报案为手机号、其余为对应的登陆账号 */
  @get:Column(length = 50)
  var authorId: String? by holder
  /** 报案信息的处理状态 */
  @get:Convert(converter = DraftStatusConverter::class)
  var draftStatus: DraftStatus? by holder

  //== 登记相关 ==
  /** 登记时间，等于首次将登记信息提交审核的时间 */
  var registerTime: OffsetDateTime? by holder
  /** 是否逾期登记 */
  var overdueRegister: Boolean? by holder
  /** 登记信息的处理状态 */
  @get:Convert(converter = AuditStatusConverter::class)
  var registerStatus: AuditStatus? by holder
  /** 登记信息的审核次数 */
  var registerCheckedCount: Int? by holder
  /** 登记信息最后一次审核的审核意见 */
  var registerCheckedComment: String? by holder
  /** 登记信息最后一次审核的审核附件 */
  @get:Convert(converter = AttachmentsConverter::class)
  var registerCheckedAttachments: List<Attachment>? by holder

  //== 报告相关 ==
  /** 报告时间，等于首次提交审核的时间 */
  var reportTime: OffsetDateTime? by holder
  /** 是否逾期报告 */
  var overdueReport: Boolean? by holder
  /** 报告信息的处理状态 */
  @get:Convert(converter = AuditStatusConverter::class)
  var reportStatus: AuditStatus? by holder
  /** 报告信息的审核次数 */
  var reportCheckedCount: Int? by holder
  /** 报告信息最后一次审核的审核意见 */
  var reportCheckedComment: String? by holder
  /** 报告信息最后一次审核的审核附件 */
  @get:Convert(converter = AttachmentsConverter::class)
  var reportCheckedAttachments: List<Attachment>? by holder
}