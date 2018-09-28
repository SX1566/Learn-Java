package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.po.AccidentCase
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [DriverType] 的 [AttributeConverter] 实现。
 * 实现 [司机驾驶状态][AccidentCase.driverType] 属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class DriverTypeConverter : AttributeConverter<DriverType?, Short?> {
  override fun convertToDatabaseColumn(attribute: DriverType?): Short? {
    return attribute?.value()
  }

  override fun convertToEntityAttribute(dbData: Short?): DriverType? {
    return dbData?.run {
      DriverType.values().find { it.value() == dbData }
        ?: throw IllegalArgumentException("无法转换值 '$dbData' 为 ${DriverType::class.simpleName}。")
    }
  }
}