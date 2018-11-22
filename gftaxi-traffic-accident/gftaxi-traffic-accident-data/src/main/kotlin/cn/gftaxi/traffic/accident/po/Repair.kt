package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.common.DynamicBean
import cn.gftaxi.traffic.accident.common.IdEntity
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
/**
 *
 */
@Entity
@Table(name = "gf_accident_repair")
class Repair : IdEntity, DynamicBean(){
  /** 维修 ID */
  @get:Id
  override var id: Int? by holder
  /** 维修主体状态 */





}