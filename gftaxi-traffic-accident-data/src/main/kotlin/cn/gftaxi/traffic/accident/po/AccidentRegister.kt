package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.converter.AccidentRegisterStatusConverter
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction.CASCADE
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.FetchType.EAGER

/**
 * 事故登记 PO。
 *
 * @author JF
 * @author RJ
 */
@Entity
@Table(
  name = "gf_accident_register",
  uniqueConstraints = [UniqueConstraint(columnNames = ["carPlate", "happenTime"])]
)
data class AccidentRegister(
  /** 等于事故报案的 ID */
  @Id
  val id: Int? = null,
  /** 状态 */
  @Convert(converter = AccidentRegisterStatusConverter::class)
  val status: Status,
  /** 事故车号，如 "粤A.12345 */
  @Column(length = 10)
  val carPlate: String,
  /** 事故车辆ID，对应BC系统车辆ID */
  val carId: Int? = null,
  /** 事发车队名称 */
  @Column(length = 10)
  val motorcadeName: String? = null,

  //== 车辆冗余信息开始 ==
  /** 车辆车型，如"现代 BH7183MY" */
  @Column(length = 50)
  val carModel: String? = null,
  /** 车辆投产日期 */
  val carOperateDate: LocalDate? = null,
  /** 合同性质 */
  @Column(length = 50)
  val carContractType: String? = null,
  /** 承包司机 */
  @Column(length = 50)
  val carContractDrivers: String? = null,
  //== 车辆冗余信息结束 ==

  /** 当事司机姓名 */
  @Column(length = 10)
  val driverName: String,
  /** 当事司机驾驶状态 */
  @Convert(converter = DriverTypeConverter::class)
  val driverType: DriverType? = null,
  /** 当事司机ID，对应BC系统司机ID，非编司机则为 null */
  val driverId: Int? = null,
  /** 紧急联系人姓名 */
  @Column(length = 50)
  val driverLinkmanName: String? = null,
  /** 紧急联系人电话 */
  @Column(length = 50)
  val driverLinkmanPhone: String? = null,

  //== 司机冗余信息开始 ==
  /** 入职日期 */
  @Column(length = 50)
  val driverHiredDate: LocalDate? = null,
  /** 联系电话 */
  @Column(length = 50)
  val driverPhone: String? = null,
  /** 身份证号 */
  @Column(length = 50)
  val driverIdentityCode: String? = null,
  /** 服务资格证号 */
  @Column(length = 50)
  val driverServiceCode: String? = null,
  /** 籍贯 */
  @Column(length = 255)
  val driverOrigin: String? = null,
  /** 年龄 */
  @Column(precision = 4, scale = 1)
  val driverAge: BigDecimal? = null,
  /** 初领驾证日期 */
  val driverLicenseDate: LocalDate? = null,
  /** 驾龄(年) */
  @Column(precision = 4, scale = 1)
  val driverDriveYears: BigDecimal? = null,
  /** 当事司机图片ID，"S:"前缀-BC司机UID、"C:"前缀-自定义图片ID*/
  val driverPicId: String? = null,
  //== 司机冗余信息结束 ==

  /** 事发时间 */
  val happenTime: OffsetDateTime,
  /** 事发经过 */
  val describe: String? = null,
  /** 登记时间，等于首次提交审核的时间 */
  val registerTime: OffsetDateTime? = null,
  /** 是否逾期登记 */
  val overdue: Boolean? = null,

  // 事发地点
  /** 事发地点的省级 */
  @Column(length = 50)
  val locationLevel1: String? = null,
  /** 事发地点的地级 */
  @Column(length = 50)
  val locationLevel2: String? = null,
  /** 事发地点的县级 */
  @Column(length = 50)
  val locationLevel3: String? = null,
  /** 事发地点的县级下面的详细地点 */
  @Column(length = 255)
  val location: String,
  /** GPS车速，km/h */
  val gpsSpeed: Short? = null,

  // 处理部门相关
  /** 处理部门 */
  @Column(length = 50)
  val dealDepartment: String? = null,
  /** 处理方式 */
  @Column(length = 50)
  val dealWay: String? = null,

  // 保险相关
  /** 保险公司 */
  @Column(length = 50)
  val insuranceCompany: String? = null,
  /** 保险报案编号 */
  @Column(length = 50)
  val insuranceCode: String? = null,

  //== 分类信息开始 ==
  /** 事故等级 */
  @Column(length = 50)
  val level: String? = null,
  /** 载重状态 */
  @Column(length = 50)
  val loadState: String? = null,
  /** 事故形态 */
  @Column(length = 50)
  val hitForm: String? = null,
  /** 碰撞类型 */
  @Column(length = 50)
  val hitType: String? = null,
  /** 天气情况 */
  @Column(length = 50)
  val weather: String? = null,
  /** 行驶方向 */
  @Column(length = 50)
  val drivingDirection: String? = null,
  /** 光线条件 */
  @Column(length = 50)
  val light: String? = null,
  /** 道路类型 */
  @Column(length = 50)
  val roadType: String? = null,
  /** 路面状况 */
  @Column(length = 50)
  val roadStructure: String? = null,
  /** 路表状况 */
  @Column(length = 50)
  val roadState: String? = null,
  //== 分类信息结束 ==

  //== 历史统计（从事发日向前推一年期间当事司机的统计）开始 ==
  /** 历史事故宗数，不包含本宗 */
  val historyAccidentCount: Short? = null,
  /** 历史交通违法次数 */
  val historyTrafficOffenceCount: Short? = null,
  /** 历史营运违章次数 */
  val historyServiceOffenceCount: Short? = null,
  /** 历史服务投诉次数 */
  val historyComplainCount: Short? = null
  //== 历史统计结束 ==
) {
  // 当事车辆列表
  @OnDelete(action = CASCADE) // 加上这个自动建表语句才会有 ON DELETE CASCADE
  @OneToMany(fetch = EAGER, cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @OrderBy("sn asc")
  var cars: Set<AccidentCar>? = null

  // 当事人列表
  @OnDelete(action = CASCADE)
  @OneToMany(fetch = EAGER, cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @OrderBy("sn asc")
  var peoples: Set<AccidentPeople>? = null

  // 其他物体列表
  @OnDelete(action = CASCADE)
  @OneToMany(fetch = EAGER, cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @OrderBy("sn asc")
  var others: Set<AccidentOther>? = null

  companion object {
    /** 查询角色 */
    const val ROLE_READ = "ACCIDENT_REGISTER_READ"
    /** 提交角色 */
    const val ROLE_SUBMIT = "ACCIDENT_REGISTER_SUBMIT"
    /** 修改角色 */
    const val ROLE_MODIFY = "ACCIDENT_REGISTER_MODIFY"
    /** 审核角色 */
    const val ROLE_CHECK = "ACCIDENT_REGISTER_CHECK"
    /** 有查阅权限的相关角色，包括 [ROLE_READ]、[ROLE_SUBMIT]、[ROLE_MODIFY]、[ROLE_CHECK] */
    val READ_ROLES = arrayOf(ROLE_READ, ROLE_SUBMIT, ROLE_MODIFY, ROLE_CHECK)

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
     * 待登记、待提交、草稿。
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

  /**
   * 司机驾驶状态。
   */
  enum class DriverType(private val value: Short) {
    /**
     * 正班。
     */
    Official(1),
    /**
     * 替班。
     */
    Shift(2),
    /**
     * 非编。
     */
    Outside(4);

    fun value(): Short {
      return value
    }
  }
}