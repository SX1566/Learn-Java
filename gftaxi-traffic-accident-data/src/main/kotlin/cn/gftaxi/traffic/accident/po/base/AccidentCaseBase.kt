package cn.gftaxi.traffic.accident.po.base

import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.common.DynamicBean
import cn.gftaxi.traffic.accident.common.IdEntity
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 案件基础信息 PO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentCaseBase : IdEntity, DynamicBean() {
  /** 案件 ID */
  @get:javax.persistence.Id
  @get:org.springframework.data.annotation.Id
  @get:GeneratedValue(strategy = GenerationType.IDENTITY)
  override var id: Int? by holder
  /** 案件编号 */
  @get:Column(length = 11, nullable = false)
  var code: String? by holder

  //== 当事车辆信息开始 ==
  /** 事故车号，如 "粤A.12345 */
  @get:Column(length = 10, nullable = false)
  var carPlate: String? by holder
  /** 事故车辆ID，对应BC系统车辆ID */
  var carId: Int? by holder
  /** 事发车队名称 */
  @get:Column(length = 10)
  var motorcadeName: String? by holder

  //== 车辆冗余信息开始 ==
  /** 车辆车型，如"现代 BH7183MY" */
  @get:Column(length = 50)
  var carModel: String? by holder
  /** 车辆投产日期 */
  var carOperateDate: LocalDate? by holder
  /** 合同性质 */
  @get:Column(length = 50)
  var carContractType: String? by holder
  /** 承包司机 */
  @get:Column(length = 50)
  var carContractDrivers: String? by holder
  //== 车辆冗余信息结束 ==
  //== 当事车辆信息结束 ==

  //== 当事司机信息开始 ==
  /** 当事司机姓名 */
  @get:Column(length = 10, nullable = false)
  var driverName: String? by holder
  /** 当事司机驾驶状态 */
  @get:Convert(converter = DriverTypeConverter::class)
  var driverType: DriverType? by holder
  /** 当事司机ID，对应BC系统司机ID，非编司机则为 null */
  var driverId: Int? by holder
  /** 紧急联系人姓名 */
  @get:Column(length = 50)
  var driverLinkmanName: String? by holder
  /** 紧急联系人电话 */
  @get:Column(length = 50)
  var driverLinkmanPhone: String? by holder

  //== 司机冗余信息开始 ==
  /** 入职日期 */
  @get:Column(length = 50)
  var driverHiredDate: LocalDate? by holder
  /** 联系电话 */
  @get:Column(length = 50)
  var driverPhone: String? by holder
  /** 身份证号 */
  @get:Column(length = 50)
  var driverIdentityCode: String? by holder
  /** 服务资格证号 */
  @get:Column(length = 50)
  var driverServiceCode: String? by holder
  /** 籍贯 */
  @get:Column(length = 255)
  var driverOrigin: String? by holder
  /** 年龄 */
  @get:Column(precision = 4, scale = 1)
  var driverAge: BigDecimal? by holder
  /** 初领驾证日期 */
  var driverLicenseDate: LocalDate? by holder
  /** 驾龄(年) */
  @get:Column(precision = 4, scale = 1)
  var driverDriveYears: BigDecimal? by holder
  /** 当事司机图片ID，"S:"前缀-BC司机UID、"C:"前缀-自定义图片ID*/
  var driverPicId: String? by holder
  //== 司机冗余信息结束 ==
  //== 当事司机信息结束 ==

  /** 事发时间 */
  @get:Column(nullable = false)
  var happenTime: OffsetDateTime? by holder
  /** 事发经过 */
  var describe: String? by holder

  // 事发地点
  /** 事发地点的省级 */
  @get:Column(length = 50)
  var locationLevel1: String? by holder
  /** 事发地点的地级 */
  @get:Column(length = 50)
  var locationLevel2: String? by holder
  /** 事发地点的县级 */
  @get:Column(length = 50)
  var locationLevel3: String? by holder
  /** 事发地点的县级下面的详细地点 */
  @get:Column(length = 255, nullable = false)
  var location: String? by holder
  /** GPS车速，km/h */
  var gpsSpeed: Short? by holder

  // 处理部门相关
  /** 处理部门 */
  @get:Column(length = 50)
  var dealDepartment: String? by holder
  /** 处理方式 */
  @get:Column(length = 50)
  var dealWay: String? by holder

  /** 保险报案编号 */
  @get:Column(length = 50)
  var insuranceCode: String? by holder

  //== 分类信息开始 ==
  /** 事故等级 */
  @get:Column(length = 50)
  var level: String? by holder
  /** 载重状态 */
  @get:Column(length = 50)
  var loadState: String? by holder
  /** 事故形态 */
  @get:Column(length = 50)
  var hitForm: String? by holder
  /** 碰撞类型 */
  @get:Column(length = 50)
  var hitType: String? by holder
  /** 天气情况 */
  @get:Column(length = 50)
  var weather: String? by holder
  /** 行驶方向 */
  @get:Column(length = 50)
  var drivingDirection: String? by holder
  /** 光线条件 */
  @get:Column(length = 50)
  var light: String? by holder
  /** 道路类型 */
  @get:Column(length = 50)
  var roadType: String? by holder
  /** 路面状况 */
  @get:Column(length = 50)
  var roadStructure: String? by holder
  /** 路表状况 */
  @get:Column(length = 50)
  var roadState: String? by holder
  //== 分类信息结束 ==

  //== 历史统计开始（从事发日向前倒推一年期间当事司机的统计，不含本宗）==
  /** 历史事故宗数，不包含本宗 */
  var historyAccidentCount: Short? by holder
  /** 历史交通违法次数 */
  var historyTrafficOffenceCount: Short? by holder
  /** 历史营运违章次数 */
  var historyServiceOffenceCount: Short? by holder
  /** 历史服务投诉次数 */
  var historyComplainCount: Short? by holder
  //== 历史统计结束 ==

  //== 工作计划 ==
  /** 约定司机回队时间 */
  val appointDriverReturnTime: OffsetDateTime? by holder
  /** 司机实际回队时间 */
  val actualDriverReturnTime: OffsetDateTime? by holder
  /** 司机回队主办人姓名 */
  val driverReturnSponsorName: String? by holder
  /** 司机回队协办人姓名 */
  val driverReturnSupporterName: String? by holder
  /** 安全教育开始时间 */
  val safetyStartTime: OffsetDateTime? by holder
  /** 安全教育结束时间 */
  val safetyEndTime: OffsetDateTime? by holder
  /** 安全教育主办人姓名 */
  val safetySponsorName: String? by holder
  /** 安全教育协办人姓名 */
  val safetySupporterName: String? by holder
  /** 诫勉谈话开始时间 */
  val talkStartTime: OffsetDateTime? by holder
  /** 诫勉谈话结束时间 */
  val talkEndTime: OffsetDateTime? by holder
  /** 诫勉谈话主办人姓名 */
  val talkSponsorName: String? by holder
  /** 诫勉谈话协办人姓名 */
  val talkSupporterName: String? by holder

  //== 安全教育 ==
  /** 事故原因 */
  val caseReason: String? by holder
  /** 处理意见 */
  val safetyComment: String? by holder
  /** 事故经过描述评价 */
  val evaluateDetails: String? by holder
  /** 事故认识情度评价 */
  val evaluateAffection: String? by holder
  /** 是否采取进一步处理措施 */
  val takeFurther: Boolean? by holder

  //== 整改措施 ==
  /** 整改措施 */
  val correctiveAction: String? by holder
  /** 司机态度 */
  val driverAttitude: String? by holder
  /** 诉讼信息 */
  val lawsuit: String? by holder
}