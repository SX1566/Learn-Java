package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.common.MaintainStatus
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [MaintainStatus] 的 [MaintainStatusConverter] 实现
 *  实现[审查状态][AccidentRepair.repairStatus] 属性值与数据库持久化值之间的互转
 *
 *  @author SX
 */
@Converter(autoApply = true)
class MaintainStatusConverter : AttributeConverter<MaintainStatus?, Short?> {
  override fun convertToEntityAttribute(dbData: Short?): MaintainStatus? {
    return dbData?.run {
      MaintainStatus.values().find { it.value() == dbData }
        ?: throw  IllegalArgumentException("无法转换值'$dbData'为${MaintainStatus::class.simpleName}")
    }
  }

  override fun convertToDatabaseColumn(attribute: MaintainStatus?): Short? {
    return attribute?.value()
  }
}