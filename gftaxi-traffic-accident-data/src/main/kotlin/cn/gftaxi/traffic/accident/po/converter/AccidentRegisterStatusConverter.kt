package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [AccidentRegister.Status] 的 [AttributeConverter] 实现。
 * 实现登记状态属性值与数据库持久化值之间的互转。
 *
 * @author JF
 */
@Converter(autoApply = true)
class AccidentRegisterStatusConverter : AttributeConverter<Status, Short> {
  override fun convertToDatabaseColumn(attribute: Status): Short {
    return attribute.value()
  }

  override fun convertToEntityAttribute(dbData: Short): Status {
    return when (dbData) {
      Status.Draft.value() -> Status.Draft
      Status.ToCheck.value() -> Status.ToCheck
      Status.Rejected.value() -> Status.Rejected
      Status.Approved.value() -> Status.Approved
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 AccidentRegister.Status。")
    }
  }
}