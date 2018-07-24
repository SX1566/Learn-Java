package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [AccidentOperation.OperationType] 的 [AttributeConverter] 实现。
 * 实现操作类型属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class AccidentOperationTypeConverter : AttributeConverter<OperationType, Short> {
  override fun convertToDatabaseColumn(attribute: OperationType): Short {
    return attribute.value()
  }

  override fun convertToEntityAttribute(dbData: Short): OperationType {
    return when (dbData) {
      Creation.value() -> Creation
      Modification.value() -> Modification
      Confirmation.value() -> Confirmation
      Approval.value() -> Approval
      Rejection.value() -> Rejection
      Deletion.value() -> Deletion
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 AccidentOperation.OperationType。")
    }
  }
}