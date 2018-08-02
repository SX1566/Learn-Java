package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentCar
import cn.gftaxi.traffic.accident.po.AccidentOther
import cn.gftaxi.traffic.accident.po.AccidentPeople
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.converter.AccidentRegisterStatusConverter
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Transient

/**
 * 事故登记信息表单用 DTO。
 *
 * @author JF
 * @author RJ
 */
@Entity
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class AccidentRegisterDto4Form constructor(
  /** 事故 ID */
  @Id val id: Int? = null,

  // 车辆信息
  val carPlate: String,
  val carId: Int? = null,
  val motorcadeName: String? = null,

  // 车辆冗余信息
  val carModel: String? = null,
  val carOperateDate: LocalDate? = null,
  val carContractType: String? = null,
  val carContractDrivers: String? = null,

  // 司机信息
  val driverName: String,
  val driverId: Int? = null,
  @Convert(converter = DriverTypeConverter::class)
  val driverType: AccidentRegister.DriverType? = null,
  val driverLinkmanName: String? = null,
  val driverLinkmanPhone: String? = null,

  // 司机冗余信息
  val driverHiredDate: LocalDate? = null,
  val driverPhone: String? = null,
  val driverIdentityCode: String? = null,
  val driverServiceCode: String? = null,
  val driverOrigin: String? = null,
  val driverAge: BigDecimal? = null,
  val driverLicenseDate: LocalDate? = null,
  val driverDriveYears: BigDecimal? = null,
  val driverPicId: String? = null,

  // 事故信息
  val code: String,
  @Convert(converter = AccidentRegisterStatusConverter::class)
  val status: AccidentRegister.Status,
  /** 报案时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  val draftTime: OffsetDateTime,
  /** 事发时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  val happenTime: OffsetDateTime,
  val locationLevel1: String? = null,
  val locationLevel2: String? = null,
  val locationLevel3: String? = null,
  val locationOther: String,
  val gpsSpeed: Short? = null,
  val describe: String? = null,
  val dealDepartment: String? = null,
  val dealWay: String? = null,
  val insuranceCompany: String? = null,
  val insuranceCode: String? = null,

  // 分类标准
  val loadState: String? = null,
  val level: String? = null,
  val hitForm: String? = null,
  val hitType: String? = null,
  val weather: String? = null,
  val drivingDirection: String? = null,
  val light: String? = null,
  val loadType: String? = null,
  val roadStructure: String? = null,
  val roadState: String? = null,

  // 历史统计
  val historyAccidentCount: Short? = null,
  val historyTrafficOffenceCount: Short? = null,
  val historyServiceOffenceCount: Short? = null,
  val historyComplainCount: Short? = null,

  // 当事车辆列表
  @Transient
  val cars: List<AccidentCar>? = null,

  // 当事人列表
  @Transient
  val peoples: List<AccidentPeople>? = null,

  // 其他物体列表
  @Transient
  val others: List<AccidentOther>? = null
)