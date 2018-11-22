package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.common.MotorcadeStatus
import cn.gftaxi.traffic.accident.po.AccidentCase
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [MotorcadeStatus] 的 [AttributeConverter] 实现。
 * 实现 [车队状态][BranchMotorcadeDto4Select.MotorcadeStatus] 属性值与数据库持久化值之间的互转。
 *
 * @author jw
 */
@Converter(autoApply = true)
class MotorcadeStatusConverter : AttributeConverter<MotorcadeStatus?, Short?> {
  override fun convertToDatabaseColumn(attribute: MotorcadeStatus?): Short? {
    return attribute?.value()
  }

  override fun convertToEntityAttribute(dbData: Short?): MotorcadeStatus? {
    return dbData?.run {
      MotorcadeStatus.values().find { it.value() == dbData }
        ?: throw IllegalArgumentException("无法转换值 '$dbData' 为 ${MotorcadeStatus::class.simpleName}。")
    }
  }
}