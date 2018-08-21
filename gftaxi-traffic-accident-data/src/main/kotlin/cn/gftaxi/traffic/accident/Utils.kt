package cn.gftaxi.traffic.accident

import cn.gftaxi.traffic.accident.dto.AccidentCarDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentOtherDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentPeopleDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.po.*
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 事故工具类。
 *
 * @author RJ
 */
object Utils {
  /** 格式化日期为 yyyy-MM-dd HH:mm 格式的处理器 */
  val FORMAT_DATE_TIME_TO_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  /** 格式化日期为 yyyyMMdd 格式的处理器 */
  val FORMAT_TO_YYYYMMDD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  /**
   * 将车号改造为 "粤A123456" 格式。
   *
   * 1. "粤A.123456"  to "粤A123456"
   * 2. "粤A•123456"  to "粤A123456"
   * 3. "粤A・123456" to "粤A123456"
   * 4. "Q2M45"      to "Q2M45"
   */
  fun polishCarPlate(carPlate: String): String {
    return carPlate.replace("•", "").replace("・", "").replace(".", "")
  }

  private const val YEAR_SECONDS: Long = 365 * 24 * 60 * 60
  /** 计算两个时间之间的年份数 */
  fun calculateYears(start: Instant, end: Instant): Float {
    return (end.epochSecond - start.epochSecond).toFloat().div(YEAR_SECONDS)
  }

  /** 计算两个时间之间的年份数 */
  fun calculateYears(start: LocalDate, end: OffsetDateTime): Float {
    return calculateYears(start.atStartOfDay(ZoneId.systemDefault()).toInstant(), end.toInstant())
  }

  /**
   * 转换 [AccidentRegister] 为 [AccidentRegisterDto4Form]。
   */
  fun convert(register: AccidentRegister, draft: AccidentDraft): AccidentRegisterDto4Form {
    return AccidentRegisterDto4Form(
      id = register.id,

      // 车辆信息
      carPlate = register.carPlate,
      carId = register.carId,
      motorcadeName = register.motorcadeName,

      // 车辆冗余信息
      carModel = register.carModel,
      carOperateDate = register.carOperateDate,
      carContractType = register.carContractType,
      carContractDrivers = register.carContractDrivers,

      // 司机信息
      driverName = register.driverName,
      driverId = register.driverId,
      driverType = register.driverType,
      driverLinkmanName = register.driverLinkmanName,
      driverLinkmanPhone = register.driverLinkmanPhone,

      // 司机冗余信息
      driverHiredDate = register.driverHiredDate,
      driverPhone = register.driverPhone,
      driverIdentityCode = register.driverIdentityCode,
      driverServiceCode = register.driverServiceCode,
      driverOrigin = register.driverOrigin,
      driverAge = register.driverAge,
      driverLicenseDate = register.driverLicenseDate,
      driverDriveYears = register.driverDriveYears,
      driverPicId = register.driverPicId,

      // 事故信息
      code = draft.code,
      status = register.status,
      draftTime = draft.reportTime,
      happenTime = register.happenTime,
      locationLevel1 = register.locationLevel1,
      locationLevel2 = register.locationLevel2,
      locationLevel3 = register.locationLevel3,
      location = register.location,
      gpsSpeed = register.gpsSpeed,
      describe = register.describe,
      dealDepartment = register.dealDepartment,
      dealWay = register.dealWay,
      insuranceCompany = register.insuranceCompany,
      insuranceCode = register.insuranceCode,

      // 分类标准
      loadState = register.loadState,
      level = register.level,
      hitForm = register.hitForm,
      hitType = register.hitType,
      weather = register.weather,
      drivingDirection = register.drivingDirection,
      light = register.light,
      roadType = register.roadType,
      roadStructure = register.roadStructure,
      roadState = register.roadState,

      // 历史统计
      historyAccidentCount = register.historyAccidentCount,
      historyTrafficOffenceCount = register.historyTrafficOffenceCount,
      historyServiceOffenceCount = register.historyServiceOffenceCount,
      historyComplainCount = register.historyComplainCount,

      // 当事车辆列表
      cars = register.cars?.map { convert(it) },

      // 当事人列表
      peoples = register.peoples?.map { convert(it) },

      // 其他物体列表
      others = register.others?.map { convert(it) }
    )
  }

  /**
   * 转换 [AccidentCar] 为 [AccidentCarDto4Update]。
   */
  fun convert(po: AccidentCar): AccidentCarDto4Update {
    val dto = AccidentCarDto4Update()
    dto.id = po.id
    dto.sn = po.sn
    dto.name = po.name
    dto.type = po.type
    dto.model = po.model
    dto.towCount = po.towCount
    dto.towMoney = po.towMoney
    dto.repairType = po.repairType
    dto.repairMoney = po.repairMoney
    dto.damageState = po.damageState
    dto.damageMoney = po.damageMoney
    dto.followType = po.followType
    dto.updatedTime = po.updatedTime
    return dto
  }

  /**
   * 转换 [AccidentPeople] 为 [AccidentPeopleDto4Update]。
   */
  fun convert(po: AccidentPeople): AccidentPeopleDto4Update {
    val dto = AccidentPeopleDto4Update()
    dto.id = po.id
    dto.sn = po.sn
    dto.name = po.name
    dto.type = po.type
    dto.sex = po.sex
    dto.phone = po.phone
    dto.transportType = po.transportType
    dto.duty = po.duty
    dto.damageState = po.damageState
    dto.damageMoney = po.damageMoney
    dto.treatmentMoney = po.treatmentMoney
    dto.compensateMoney = po.compensateMoney
    dto.followType = po.followType
    dto.updatedTime = po.updatedTime
    return dto
  }

  /**
   * 转换 [AccidentOther] 为 [AccidentOtherDto4Update]。
   */
  fun convert(po: AccidentOther): AccidentOtherDto4Update {
    val dto = AccidentOtherDto4Update()
    dto.id = po.id
    dto.sn = po.sn
    dto.name = po.name
    dto.type = po.type
    dto.belong = po.belong
    dto.linkmanName = po.linkmanName
    dto.linkmanPhone = po.linkmanPhone
    dto.damageState = po.damageState
    dto.damageMoney = po.damageMoney
    dto.actualMoney = po.actualMoney
    dto.followType = po.followType
    dto.updatedTime = po.updatedTime
    return dto
  }
}