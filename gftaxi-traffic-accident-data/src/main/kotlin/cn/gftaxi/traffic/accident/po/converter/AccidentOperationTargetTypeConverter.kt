package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [AccidentOperation.TargetType] 的 [AttributeConverter] 实现。
 * 实现事务类型属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class AccidentOperationTargetTypeConverter : AttributeConverter<TargetType, Short> {
  override fun convertToDatabaseColumn(attribute: TargetType): Short {
    return attribute.value()
  }

  override fun convertToEntityAttribute(dbData: Short): TargetType {
    return when (dbData) {
      Draft.value() -> Draft
      Register.value() -> Register
      Report.value() -> Report
      Handle.value() -> Handle
      Close.value() -> Close
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 AccidentOperation.TargetType。")
    }
  }
}