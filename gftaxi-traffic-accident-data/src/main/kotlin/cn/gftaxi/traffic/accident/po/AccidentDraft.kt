package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.converter.AccidentDraftStatusConverter
import java.io.Serializable
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.*

/**
 * 事故报案 PO。
 *
 * @author cjw
 * @author RJ
 */
@Entity
@Table(
  name = "gf_accident_draft ",
  uniqueConstraints = [UniqueConstraint(columnNames = ["car_plate", "happen_time"])]
)
data class AccidentDraft(
  /**
   * 事故编号
   * 格式为 yyyyMMdd_nn'
   * */
  @Id
  @Column(length = 11)
  val code: String,
  /** 状态 */
  @Convert(converter = AccidentDraftStatusConverter::class)
  val status: Status,
  /** 事故车号，如 "粤A123456" */
  @Column(length = 8)
  val carPlate: String,
  /** 当事司机 */
  @Column(length = 8)
  val driverName: String,
  /** 事发时间 */
  val happenTime: OffsetDateTime,
  /** 接案时间 */
  val reportTime: OffsetDateTime,
  /** 事发地点 */
  @Column(length = 100)
  val location: String,
  /** 事故形态 */
  @Column(length = 50)
  val hitForm: String,
  /** 碰撞类型 */
  @Column(length = 50)
  val hitType: String,
  /** 是否逾期报案 */
  val overdue: Boolean,
  /** 报案来源：BC-BC系统Web端、EMAIL-邮件、WEIXIN-微信、SMS-短信、{appId}-应用ID */
  @Column(length = 10)
  val source: String,
  /** 接案人姓名 */
  @Column(length = 50)
  val authorName: String,
  /** 接案人标识：邮件报案为邮箱、短信报案为手机号、其余为对应的登陆账号 */
  @Column(length = 50)
  val authorId: String,
  /** 简要描述 */
  val describe: String
) : Serializable {
  companion object {
    /** 查询报案信息角色 */
    const val ROLE_READ = "ACCIDENT_DRAFT_READ"
    /** 提交报案信息角色 */
    const val ROLE_SUBMIT = "ACCIDENT_DRAFT_SUBMIT"
    /** 提交报案信息角色 */
    const val ROLE_MODIFY = "ACCIDENT_DRAFT_MODIFY"

    /** 判断是否是预期报案 */
    fun isOverdue(happenTime: OffsetDateTime, reportTime: OffsetDateTime, overdueSeconds: Long): Boolean {
      return Duration.between(happenTime, reportTime).get(ChronoUnit.SECONDS) > overdueSeconds
    }
  }

  /**
   * 案件状态。
   */
  enum class Status(private val value: Short) {
    /**
     * 待登记。
     */
    Todo(1),
    /**
     * 已登记。
     */
    Done(2);

    fun value(): Short {
      return value
    }
  }
}