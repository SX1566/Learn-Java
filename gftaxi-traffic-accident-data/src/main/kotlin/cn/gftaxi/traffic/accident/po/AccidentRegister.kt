package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.converter.AccidentRegisterStatusConverter
import java.math.BigDecimal
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.*

/**
 * 事故登记 PO。
 *
 * @author JF
 */
@Entity
@Table(
  name = "gf_accident_register",
  uniqueConstraints = [UniqueConstraint(columnNames = ["carPlate", "happenTime"])]
)
data class AccidentRegister(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Int?,
  /** 事故编号，格式为 yyyyMMdd_nn */
  @Column(unique = true, length = 11)
  val code: String,
  /** 状态 */
  @Convert(converter = AccidentRegisterStatusConverter::class)
  val status: Status,
  /** 车辆ID，对应BC系统车辆ID */
  val carId: Int,
  /** 事故车号，如 "粤A123456 */
  @Column(length = 8)
  val carPlate: String,
  /** 事发车队 */
  @Column(length = 8)
  val motorcadeName: String,
  /** 当事司机姓名 */
  @Column(length = 8)
  val partyDriverName: String,
  /** 当事司机是否是非编司机 */
  val outsideDriver: Boolean,
  /** 当班司机ID，对应BC系统司机ID */
  val dutyDriverId: Int,
  /** 当班司机姓名 */
  @Column(length = 8)
  val dutyDriverName: String,
  /** 事发时间 */
  val happenTime: OffsetDateTime,
  /** 事发经过 */
  val describe: String,
  // 分类信息
  /** 事故形态 */
  @Column(length = 50)
  val hitForm: String,
  /** 碰撞类型 */
  @Column(length = 50)
  val hitType: String,
  /** 天气情况 */
  @Column(length = 50)
  val weather: String,
  /** 光线条件 */
  @Column(length = 50)
  val light: String,
  /** 行驶方向 */
  @Column(length = 50)
  val drivingDirection: String,
  /** 道路类型 */
  @Column(length = 50)
  val roadType: String,
  /** 路面状况 */
  @Column(length = 50)
  val roadStructure: String,
  /** 路表状况 */
  @Column(length = 50)
  val roadState: String,

  // 当事车辆、人、物的数量
  /** 当事车数 */
  val carCount: Short,
  /** 当事人数 */
  val peopleCount: Short,
  /** 其他物体数 */
  val otherCount: Short,

  // 处理部门相关
  /** 处理部门 */
  @Column(length = 50)
  val dealDepartment: String,
  /** 处理方式 */
  @Column(length = 50)
  val dealWay: String,

  // 保险相关
  /** 保险公司 */
  @Column(length = 50)
  val insuranceCompany: String,
  /** 保险报案编号 */
  @Column(length = 50)
  val insuranceCode: String,

  // 事发地点
  /** 事发地点的省级 */
  @Column(length = 50)
  val locationLevel1: String,
  /** 事发地点的地级 */
  @Column(length = 50)
  val locationLevel2: String,
  /** 事发地点的县级 */
  @Column(length = 50)
  val locationLevel3: String,
  /** 事发地点的县级下面的详细地点 */
  @Column(length = 255)
  val locationOther: String,
  /** 事发地点的经度 */
  @Column(precision = 9, scale = 6)
  val gpsLongitude: BigDecimal,
  /** 事发地点的纬度 */
  @Column(precision = 9, scale = 6)
  val gpsLatitude: BigDecimal,
  /** GPS车速，km/h */
  val gpsSpeed: Short,

  // 报案、登记的相关标记
  /** 登记时间，等于首次提交审核的时间 */
  val registerTime: OffsetDateTime,
  /** 是否逾期登记 */
  val overdueRegister: Boolean,
  /** 报案时间 */
  val draftTime: OffsetDateTime
) {
  companion object {
    /** 查询角色 */
    const val ROLE_READ = "ACCIDENT_REGISTER_READ"
    /** 提交角色 */
    const val ROLE_SUBMIT = "ACCIDENT_REGISTER_SUBMIT"
    /** 修改角色 */
    const val ROLE_MODIFY = "ACCIDENT_REGISTER_MODIFY"
    /** 审核角色 */
    const val ROLE_CHECK = "ACCIDENT_REGISTER_CHECK"

    /** 判断是否是预期登记 */
    fun isOverdue(happenTime: OffsetDateTime, registerTime: OffsetDateTime, overdueSeconds: Long): Boolean {
      return Duration.between(happenTime, registerTime).get(ChronoUnit.SECONDS) > overdueSeconds
    }
  }

  /**
   * 登记状态。
   */
  enum class Status(private val value: Short) {
    /**
     * 待提交。
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