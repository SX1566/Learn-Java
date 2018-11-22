package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.MaintainStatus
import cn.gftaxi.traffic.accident.po.converter.MaintainStatusConverter
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

/** 维修登记视图用DTO
 *
 * @author SX
 */
@Entity
data class RepairDto4View constructor(
  @Id
  val id: Int? = null,
  val code: String? = null,

  /** 登记状态 */
  @Convert(converter = MaintainStatusConverter::class)
  val repairStatus: MaintainStatus? = null

)