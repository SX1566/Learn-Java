package cn.gftaxi.traffic.accident.bc.dto

import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.common.Utils.calculateYears
import cn.gftaxi.traffic.accident.po.AccidentCase
import java.io.Serializable
import java.time.LocalDate

/**
 * BC 系统案件相关信息 DTO。
 *
 * @author RJ
 */
data class CaseRelatedInfoDto constructor(
  //== 事发时的信息开始 ==
  /** 事发时所属车队 */
  val motorcadeName: String? = null,
  /** 事发时的合同性质 */
  val contractType: String? = null,
  /** 事发时的承包司机，多个司机用英文逗号连接在一起 */
  val contractDrivers: String? = null,

  //== 当事车辆信息开始 ==
  /** 车辆ID，对应BC系统车辆ID */
  val carId: Int? = null,
  /** 车辆车型，如"现代 BH7183MY" */
  val carModel: String? = null,
  /** 车辆投产日期 */
  val carOperateDate: LocalDate? = null,
  //== 当事车辆信息结束 ==

  //== 当事司机信息开始 ==
  /** 当事司机ID，对应BC系统司机ID，非编司机则为 null */
  val driverId: Int? = null,
  val driverUid: String? = null,
  /** 驾驶状态 */
  val driverType: DriverType? = null,
  /** 联系电话 */
  val driverPhone: String? = null,
  /** 入职日期 */
  val driverHiredDate: LocalDate? = null,
  /** 出生日期 */
  val driverBirthDate: LocalDate? = null,
  /** 身份证号 */
  val driverIdentityCode: String? = null,
  /** 服务资格证号 */
  val driverServiceCode: String? = null,
  /** 籍贯 */
  val driverOrigin: String? = null,
  /** 初领驾证日期 */
  val driverLicenseDate: LocalDate? = null,
  /** 对班司机姓名，双班营运时才有 */
  val relatedDriverName: String? = null,
  /** 对班司机联系电话，双班营运时才有 */
  val relatedDriverPhone: String? = null,
  //== 当事司机信息结束 ==

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
) : Serializable {
  fun copyTo(case: AccidentCase) {
    // 车辆信息
    case.motorcadeName = motorcadeName
    case.carContractType = contractType
    case.carContractDrivers = contractDrivers
    case.carId = carId
    case.carModel = carModel
    case.carOperateDate = carOperateDate

    // 司机开始
    case.driverId = driverId
    case.driverPicId = driverUid?.let { "S:$it" }
    case.driverType = driverType
    case.driverPhone = driverPhone
    case.driverHiredDate = driverHiredDate
    case.driverAge = driverBirthDate?.let { calculateYears(it, LocalDate.now()).toBigDecimal() }
    case.driverIdentityCode = driverIdentityCode
    case.driverServiceCode = driverServiceCode
    case.driverOrigin = driverOrigin
    case.driverLicenseDate = driverLicenseDate
    case.driverDriveYears = driverLicenseDate?.let { calculateYears(it, LocalDate.now()).toBigDecimal() }
    case.driverLinkmanName = relatedDriverName?.let { "[对班] $it" }
    case.driverLinkmanPhone = relatedDriverPhone?.let { "[对班] $it" }

    // 历史统计
    case.historyAccidentCount = historyAccidentCount
    case.historyTrafficOffenceCount = historyTrafficOffenceCount
    case.historyServiceOffenceCount = historyServiceOffenceCount
    case.historyComplainCount = historyComplainCount
  }
}