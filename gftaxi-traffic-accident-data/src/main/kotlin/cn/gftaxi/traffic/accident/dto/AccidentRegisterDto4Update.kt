package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * 更新事故登记信息用动态 DTO。
 *
 * 特别注意类中的属性需要定义为非只读属性 `var` 而不是 `val`，并且全部属性需要定义为 nullable，且不要设置任何默认值。
 *
 * @author RJ
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AccidentRegisterDto4Update(
  @JsonIgnore
  var changedProperties: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { null }
) {
  // 车辆信息
  var carPlate: String? by changedProperties
  var carId: Int? by changedProperties
  var motorcadeName: String? by changedProperties

  // 车辆冗余信息
  var carModel: String?  by changedProperties
  @get:JsonFormat(pattern = "yyyy-MM-dd")
  @set:JsonFormat(pattern = "yyyy-MM-dd")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd")
  var carOperateDate: LocalDate?  by changedProperties
  var carContractType: String?  by changedProperties
  var carContractDrivers: String?  by changedProperties

  // 司机信息
  var driverName: String?  by changedProperties
  var driverId: Int?  by changedProperties
  var driverType: AccidentRegister.DriverType?  by changedProperties
  var driverLinkmanName: String?  by changedProperties
  var driverLinkmanPhone: String?  by changedProperties

  // 司机冗余信息
  @get:JsonFormat(pattern = "yyyy-MM-dd")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd")
  var driverHiredDate: LocalDate?  by changedProperties
  var driverPhone: String?  by changedProperties
  var driverIdentityCode: String?  by changedProperties
  var driverServiceCode: String?  by changedProperties
  var driverOrigin: String?  by changedProperties
  var driverAge: BigDecimal?  by changedProperties
  @get:JsonFormat(pattern = "yyyy-MM-dd")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd")
  var driverLicenseDate: LocalDate?  by changedProperties
  var driverDriveYears: BigDecimal?  by changedProperties
  var driverPicId: String?  by changedProperties

  // 事故信息
  @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  @set:DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  var happenTime: OffsetDateTime?  by changedProperties
  var locationLevel1: String?  by changedProperties
  var locationLevel2: String?  by changedProperties
  var locationLevel3: String?  by changedProperties
  var location: String?  by changedProperties
  var gpsSpeed: Short?  by changedProperties
  var describe: String?  by changedProperties
  var dealDepartment: String?  by changedProperties
  var dealWay: String?  by changedProperties
  var insuranceCompany: String?  by changedProperties
  var insuranceCode: String?  by changedProperties

  // 分类标准
  var loadState: String?  by changedProperties
  var level: String?  by changedProperties
  var hitForm: String?  by changedProperties
  var hitType: String?  by changedProperties
  var weather: String?  by changedProperties
  var drivingDirection: String?  by changedProperties
  var light: String?  by changedProperties
  var loadType: String?  by changedProperties
  var roadStructure: String?  by changedProperties
  var roadState: String?  by changedProperties

  // 历史统计
  var historyAccidentCount: Short?  by changedProperties
  var historyTrafficOffenceCount: Short?  by changedProperties
  var historyServiceOffenceCount: Short?  by changedProperties
  var historyComplainCount: Short?  by changedProperties

  // 当事车辆列表
  var cars: List<AccidentCarDto4Update>? by changedProperties

  // 当事人列表
  var peoples: List<AccidentPeopleDto4Update>? by changedProperties

  // 其他物体列表
  var others: List<AccidentOtherDto4Update>? by changedProperties

  override fun toString(): String {
    return "${AccidentRegisterDto4Update::class.simpleName}=$changedProperties"
  }
}