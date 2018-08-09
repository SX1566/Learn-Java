package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Approved
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Rejected
import cn.gftaxi.traffic.accident.po.converter.AccidentRegisterStatusConverter
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 事故登记的已审核案件的最后审核信息 DTO。
 *
 * @author RJ
 */
@Entity
data class AccidentRegisterDto4LastChecked constructor(
  @Id val id: Int? = null,
  val code: String,
  val carPlate: String,
  val driverName: String,
  @Convert(converter = DriverTypeConverter::class)
  val driverType: DriverType,
  /** 事发地点 */
  val location: String,
  /** 事发车队 */
  val motorcadeName: String,
  val happenTime: OffsetDateTime,
  /** 审核意见 */
  val checkedComment: String? = null,
  /** 审核人姓名 */
  val checkerName: String,
  /** 审核次数 */
  val checkedCount: Int,
  /** 审核时间 */
  val checkedTime: OffsetDateTime,
  /** 附件 ID */
  val attachmentId: String? = null
)