package cn.gftaxi.traffic.accident.po.base

import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.common.DynamicBean
import cn.gftaxi.traffic.accident.common.FieldComment
import cn.gftaxi.traffic.accident.common.IdEntity
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.persistence.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * 案件基础信息 PO。
 *
 * @author RJ
 * @author zh
 */
@MappedSuperclass
open class AccidentCaseBase : IdEntity, DynamicBean() {
  /** 案件 ID */
  @get:javax.persistence.Id
  @get:org.springframework.data.annotation.Id
  @get:GeneratedValue(strategy = GenerationType.IDENTITY)
  override var id: Int? by holder
  @FieldComment("案件编号")
  @get:Column(length = 11, nullable = false)
  var code: String? by holder

  //== 当事车辆信息开始 ==
  @FieldComment("事故车号")
  @get:Column(length = 10, nullable = false)
  var carPlate: String? by holder
  @FieldComment("事故车辆ID")
  var carId: Int? by holder
  @FieldComment("事发车队名称")
  @get:Column(length = 10)
  var motorcadeName: String? by holder

  //== 车辆冗余信息开始 ==
  @FieldComment("车辆车型")
  @get:Column(length = 50)
  var carModel: String? by holder
  @FieldComment("车辆投产日期")
  var carOperateDate: LocalDate? by holder
  @FieldComment("合同性质")
  @get:Column(length = 50)
  var carContractType: String? by holder
  @FieldComment("承包司机")
  @get:Column(length = 50)
  var carContractDrivers: String? by holder
  //== 车辆冗余信息结束 ==
  //== 当事车辆信息结束 ==

  //== 当事司机信息开始 ==
  @FieldComment("当事司机姓名")
  @get:Column(length = 10, nullable = false)
  var driverName: String? by holder
  @FieldComment("当事司机驾驶状态")
  @get:Convert(converter = DriverTypeConverter::class)
  var driverType: DriverType? by holder
  /** 当事司机ID，对应BC系统司机ID，非编司机则为 null */
  @FieldComment("当事司机ID")
  var driverId: Int? by holder
  @FieldComment("紧急联系人姓名")
  @get:Column(length = 50)
  var driverLinkmanName: String? by holder
  @FieldComment("紧急联系人电话")
  @get:Column(length = 50)
  var driverLinkmanPhone: String? by holder

  //== 司机冗余信息开始 ==
  @FieldComment("入职日期")
  @get:Column(length = 50)
  var driverHiredDate: LocalDate? by holder
  @FieldComment("联系电话")
  @get:Column(length = 50)
  var driverPhone: String? by holder
  @FieldComment("身份证号")
  @get:Column(length = 50)
  var driverIdentityCode: String? by holder
  @FieldComment("服务资格证号")
  @get:Column(length = 50)
  var driverServiceCode: String? by holder
  @FieldComment("籍贯")
  @get:Column(length = 255)
  var driverOrigin: String? by holder
  @FieldComment("年龄")
  @get:Column(precision = 4, scale = 1)
  var driverAge: BigDecimal? by holder
  @FieldComment("初领驾证日期")
  var driverLicenseDate: LocalDate? by holder
  @FieldComment("驾龄")
  @get:Column(precision = 4, scale = 1)
  var driverDriveYears: BigDecimal? by holder
  /** 当事司机图片ID，"S:"前缀-BC司机UID、"C:"前缀-自定义图片ID*/
  @FieldComment("当事司机图片ID")
  var driverPicId: String? by holder
  //== 司机冗余信息结束 ==
  //== 当事司机信息结束 ==

  @FieldComment("事发时间")
  @get:Column(nullable = false)
  var happenTime: OffsetDateTime? by holder
  @FieldComment("事发经过")
  var describe: String? by holder

  // 事发地点
  @FieldComment("事发地点的省级")
  @get:Column(length = 50)
  var locationLevel1: String? by holder
  @FieldComment("事发地点的地级")
  @get:Column(length = 50)
  var locationLevel2: String? by holder
  @FieldComment("事发地点的县级")
  @get:Column(length = 50)
  var locationLevel3: String? by holder
  @FieldComment("事发地点的县级下面的详细地点")
  @get:Column(length = 255, nullable = false)
  var location: String? by holder
  @FieldComment("GPS车速")
  var gpsSpeed: Short? by holder

  // 处理部门相关
  @FieldComment("处理部门")
  @get:Column(length = 50)
  var dealDepartment: String? by holder
  @FieldComment("处理方式")
  @get:Column(length = 50)
  var dealWay: String? by holder
  @FieldComment("保险报案编号")
  @get:Column(length = 50)
  var insuranceCode: String? by holder

  //== 分类信息开始 ==
  @FieldComment("事故等级")
  @get:Column(length = 50)
  var level: String? by holder
  @FieldComment("载重状态")
  @get:Column(length = 50)
  var loadState: String? by holder
  @FieldComment("事故形态")
  @get:Column(length = 50)
  var hitForm: String? by holder
  @FieldComment("碰撞类型")
  @get:Column(length = 50)
  var hitType: String? by holder
  @FieldComment("天气情况")
  @get:Column(length = 50)
  var weather: String? by holder
  @FieldComment("行驶方向")
  @get:Column(length = 50)
  var drivingDirection: String? by holder
  @FieldComment("光线条件")
  @get:Column(length = 50)
  var light: String? by holder
  @FieldComment("道路类型")
  @get:Column(length = 50)
  var roadType: String? by holder
  @FieldComment("路面状况")
  @get:Column(length = 50)
  var roadStructure: String? by holder
  @FieldComment("路表状况")
  @get:Column(length = 50)
  var roadState: String? by holder
  //== 分类信息结束 ==

  //== 历史统计开始（从事发日向前倒推一年期间当事司机的统计，不含本宗）==
  @FieldComment("历史事故宗数")
  var historyAccidentCount: Short? by holder
  @FieldComment("历史交通违法次数")
  var historyTrafficOffenceCount: Short? by holder
  @FieldComment("历史营运违章次数")
  var historyServiceOffenceCount: Short? by holder
  @FieldComment("历史服务投诉次数")
  var historyComplainCount: Short? by holder
  //== 历史统计结束 ==

  //== 工作计划 ==
  @FieldComment("约定司机回队时间")
  var appointDriverReturnTime: OffsetDateTime? by holder
  @FieldComment("司机实际回队时间")
  var actualDriverReturnTime: OffsetDateTime? by holder
  @FieldComment("司机回队主办人姓名")
  var driverReturnSponsorName: String? by holder
  @FieldComment("司机回队协办人姓名")
  var driverReturnSupporterName: String? by holder
  @FieldComment("安全教育开始时间")
  var safetyStartTime: OffsetDateTime? by holder
  @FieldComment("安全教育结束时间")
  var safetyEndTime: OffsetDateTime? by holder
  @FieldComment("安全教育主办人姓名")
  var safetySponsorName: String? by holder
  @FieldComment("安全教育协办人姓名")
  var safetySupporterName: String? by holder
  @FieldComment("诫勉谈话开始时间")
  var talkStartTime: OffsetDateTime? by holder
  @FieldComment("诫勉谈话结束时间")
  var talkEndTime: OffsetDateTime? by holder
  @FieldComment("诫勉谈话主办人姓名")
  var talkSponsorName: String? by holder
  @FieldComment("诫勉谈话协办人姓名")
  var talkSupporterName: String? by holder

  //== 安全教育 ==
  @FieldComment("事故原因")
  var caseReason: String? by holder
  @FieldComment("处理意见")
  var safetyComment: String? by holder
  @FieldComment("事故经过描述评价")
  var evaluateDetails: String? by holder
  @FieldComment("事故认识情度评价")
  var evaluateAffection: String? by holder
  @FieldComment("是否采取进一步处理措施")
  var takeFurther: Boolean? by holder

  //== 整改措施 ==
  @FieldComment("整改措施")
  var correctiveAction: String? by holder
  @FieldComment("司机态度")
  var driverAttitude: String? by holder
  @FieldComment("诉讼信息")
  var lawsuit: String? by holder
}