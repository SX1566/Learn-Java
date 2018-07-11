package cn.gftaxi.traffic.accident.po

import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.OffsetDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * 事故报案
 *
 * @author cjw
 */
@Entity
@Table(
  name = "gf_accident_draft ",
  uniqueConstraints = [UniqueConstraint(columnNames = [
    "status", "car_plate", "driver_name", "happen_time", "report_time"
    , "location", "hit_form", "hit_type", "source", "author_name", "author_id"
  ])]
)
data class AccidentDraft(
  /**
   * 事故编号
   * 格式为 yyyyMMdd_nn'
   * */
  @Id
  val code: String,
  /** 状态：1-待登记、2-已登记 */
  val status: Status,
  /** 事故车号，如 "粤A123456" */
  val carPlate: String,
  /** 当事司机 */
  val driverName: String,
  /** 事发时间 */
  val happenTime: OffsetDateTime,
  /** 接案时间 */
  val reportTime: OffsetDateTime,
  /** 事发地点 */
  val location: String,
  /** 事故形态 */
  val hitForm: String,
  /** 碰撞类型 */
  val hitType: String,
  /** 是否逾期报案 */
  val overdue: Boolean,
  /** 报案来源：BC-BC系统Web端、EMAIL-邮件、WEIXIN-微信、SMS-短信、{appId}-应用ID */
  val source: String,
  /** 结案人姓名 */
  val authorName: String,
  /** 结案人标识：邮件报案为邮箱、短信报案为手机号、其余为对应的登陆账号 */
  val authorId: String,
  /** 简要描述 */
  val describe: String
) : Serializable {
  companion object {
    /** 事故报案查阅角色 */
    const val ROLE_READ = "ACCIDENT_DRAFT_READ"
    /** 事故报案管理角色 */
    const val ROLE_MANAGE = "ACCIDENT_DRAFT_MANAGE"
  }

  /**
   * 案件状态。
   */
  enum class Status(private val value: Int) {
    /**
     * 待登记。
     */
    Todo(1),
    /**
     * 已登记。
     */
    Done(2);

    fun value(): Int {
      return value
    }
  }
}
