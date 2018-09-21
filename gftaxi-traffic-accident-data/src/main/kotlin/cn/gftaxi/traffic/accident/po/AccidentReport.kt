package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.converter.AccidentReportStatusConverter
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * 事故报告 PO。
 *
 * @author RJ
 * @author zh
 */
@Entity
@Table(name = "gf_accident_report")
data class AccidentReport(
  /** 等于事故报案的 ID */
  @Id
  val id: Int? = null,
  /** 状态 */
  @Convert(converter = AccidentReportStatusConverter::class)
  val status: Status,

  /** 报告时间，等于首次提交审核的时间 */
  val reportTime: OffsetDateTime? = null,
  /** 是否逾期报告 */
  val overdueReport: Boolean? = null,
  /** 诉讼信息 */
  val lawsuit: String? = null,

  //== 工作计划 ==
  /** 约定司机回队时间 */
  val appointDriverReturnTime: OffsetDateTime? = null,
  /** 司机实际回队时间 */
  val actualDriverReturnTime: OffsetDateTime? = null,
  /** 司机回队主办人姓名 */
  val driverReturnSponsorName: String? = null,
  /** 司机回队协办人姓名 */
  val driverReturnSupporterName: String? = null,
  /** 安全教育开始时间 */
  val safetyStartTime: OffsetDateTime? = null,
  /** 安全教育结束时间 */
  val safetyEndTime: OffsetDateTime? = null,
  /** 安全教育主办人姓名 */
  val safetySponsorName: String? = null,
  /** 安全教育协办人姓名 */
  val safetySupporterName: String? = null,
  /** 诫勉谈话开始时间 */
  val talkStartTime: OffsetDateTime? = null,
  /** 诫勉谈话结束时间 */
  val talkEndTime: OffsetDateTime? = null,
  /** 诫勉谈话主办人姓名 */
  val talkSponsorName: String? = null,
  /** 诫勉谈话协办人姓名 */
  val talkSupporterName: String? = null,

  //== 安全教育 ==
  /** 事故原因 */
  val caseReason: String? = null,
  /** 处理意见 */
  val safetyComment: String? = null,
  /** 事故经过描述评价 */
  val evaluateDetails: String? = null,
  /** 事故认识情度评价 */
  val evaluateAffection: String? = null,
  /** 是否采取进一步处理措施 */
  val takeFurther: Boolean? = null,

  //== 整改措施 ==
  /** 整改措施 */
  val correctiveAction: String? = null,
  /** 司机态度 */
  val driverAttitude: String? = null
) {
  companion object {
    /** 查询角色 */
    const val ROLE_READ = "ACCIDENT_REPORT_READ"
    /** 提交角色 */
    const val ROLE_SUBMIT = "ACCIDENT_REPORT_SUBMIT"
    /** 修改角色 */
    const val ROLE_MODIFY = "ACCIDENT_REPORT_MODIFY"
    /** 审核角色 */
    const val ROLE_CHECK = "ACCIDENT_REPORT_CHECK"
    /** 有查阅权限的相关角色，包括 [ROLE_READ]、[ROLE_SUBMIT]、[ROLE_MODIFY]、[ROLE_CHECK] */
    val READ_ROLES = arrayOf(ROLE_READ, ROLE_SUBMIT, ROLE_MODIFY, ROLE_CHECK)

    /** 判断是否是逾期报告 */
    fun isOverdue(happenTime: OffsetDateTime, reportTime: OffsetDateTime, overdueSeconds: Long): Boolean {
      return Duration.between(happenTime, reportTime).get(ChronoUnit.SECONDS) > overdueSeconds
    }
  }

  /**
   * 报告状态。
   */
  enum class Status(private val value: Short) {
    /**
     * 待报告、待提交、草稿。
     */
    Draft(1),
    /**
     * 待审核。
     */
    ToCheck(2),
    /**
     * 审核不通过。
     */
    Rejected(4),
    /**
     * 审核通过。
     */
    Approved(8);

    fun value(): Short {
      return value
    }
  }
}