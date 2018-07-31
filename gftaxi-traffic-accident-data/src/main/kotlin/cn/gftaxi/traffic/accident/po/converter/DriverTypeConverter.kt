package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [DriverType] 的 [AttributeConverter] 实现。
 * 实现司机驾驶状态属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class DriverTypeConverter : AttributeConverter<DriverType?, Short?> {
  override fun convertToDatabaseColumn(attribute: DriverType?): Short? {
    return attribute?.value()
  }

  override fun convertToEntityAttribute(dbData: Short?): DriverType? {
    return when (dbData) {
      Official.value() -> Official
      Shift.value() -> Shift
      Outside.value() -> Outside
      null -> null
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 AccidentRegister.DriverType。")
    }
  }
}