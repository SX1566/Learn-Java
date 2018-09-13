package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.IdEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.persistence.MappedSuperclass

/**
 * 事故表单用的基本信息 DTO。
 *
 * 由于使用了动态 DTO 技术，要特别注意类中的属性需要定义为非只读属性 `var` 而不是 `val`，
 * 并且全部属性需要定义为可空，且不要设置任何默认值。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentCaseDto4FormBaseInfo : IdEntity, DynamicDto() {
  @get:javax.persistence.Id
  @get:org.springframework.data.annotation.Id
  override var id: Int? by data
  var code: String?  by data

  // 车辆信息
  var motorcadeName: String? by data
  var carPlate: String? by data
  var carId: Int? by data

  // 车辆冗余信息
  var carModel: String?  by data
  var carOperateDate: LocalDate?  by data
  var carContractType: String?  by data
  var carContractDrivers: String?  by data

  // 司机信息
  var driverName: String?  by data
  var driverId: Int?  by data
  var driverType: AccidentRegister.DriverType?  by data
  var driverLinkmanName: String?  by data
  var driverLinkmanPhone: String?  by data

  // 司机冗余信息
  var driverHiredDate: LocalDate?  by data
  var driverPhone: String?  by data
  var driverIdentityCode: String?  by data
  var driverServiceCode: String?  by data
  var driverOrigin: String?  by data
  var driverAge: BigDecimal?  by data
  var driverLicenseDate: LocalDate?  by data
  var driverDriveYears: BigDecimal?  by data
  var driverPicId: String?  by data

  // 事故信息
  var happenTime: OffsetDateTime?  by data
  var location: String?  by data
  var locationLevel1: String?  by data
  var locationLevel2: String?  by data
  var locationLevel3: String?  by data
  var gpsSpeed: Short?  by data
  var describe: String?  by data
  var dealDepartment: String?  by data
  var dealWay: String?  by data
  var insuranceCompany: String?  by data
  var insuranceCode: String?  by data

  // 分类标准
  var loadState: String?  by data
  var level: String?  by data
  var hitForm: String?  by data
  var hitType: String?  by data
  var weather: String?  by data
  var drivingDirection: String?  by data
  var light: String?  by data
  var roadType: String?  by data
  var roadStructure: String?  by data
  var roadState: String?  by data

  // 当事车辆
  @get:javax.persistence.Transient
  @get:org.springframework.data.annotation.Transient
  var cars: List<AccidentCarDto4Form>? by data

  // 当事人
  @get:javax.persistence.Transient
  @get:org.springframework.data.annotation.Transient
  var peoples: List<AccidentPeopleDto4Form>? by data

  // 其他物体
  @get:javax.persistence.Transient
  @get:org.springframework.data.annotation.Transient
  var others: List<AccidentOtherDto4Form>? by data
}