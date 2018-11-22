package cn.gftaxi.traffic.accident.bc.dto

import cn.gftaxi.traffic.accident.common.MotorcadeStatus
import cn.gftaxi.traffic.accident.po.converter.MotorcadeStatusConverter
import java.io.Serializable
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/**
 * 车队信息 DTO。
 *
 * @author jw
 */
@Entity
data class MotorcadeDto constructor(
  /** 车队 ID */
  @Id val id: Int,
  /** 车队名称 */
  val name: String,
  /** 排序号 */
  val sn: Int,
  /** 车队状态：Enabled 为正常，Disabled 为禁用 */
  @Convert(converter = MotorcadeStatusConverter::class)
  val status: MotorcadeStatus,
  /** 分公司 ID */
  val branchId: Int,
  /** 分公司名称 */
  val branchName: String,
  /** 车队长 ID */
  val captainId: Int,
  /** 车队长姓名 */
  val captainName: String
) : Serializable