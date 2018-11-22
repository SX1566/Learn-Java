package cn.gftaxi.traffic.accident.po.view.converter

import cn.gftaxi.traffic.accident.po.view.CarMan
import cn.gftaxi.traffic.accident.po.view.CarMan.Status
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [CarMan.Status] 的 [AttributeConverter] 实现。
 * 实现司机责任人状态属性值与数据库持久化值之间的互转。
 *
 * @author JF
 */
@Converter(autoApply = true)
class CarManStatusConverter : AttributeConverter<Status, Short> {
  override fun convertToDatabaseColumn(attribute: Status): Short {
    return attribute.value()
  }

  override fun convertToEntityAttribute(dbData: Short): Status {
    return when (dbData) {
      Status.Draft.value() -> Status.Draft
      Status.Enabled.value() -> Status.Enabled
      Status.Disabled.value() -> Status.Disabled
      Status.Deleted.value() -> Status.Deleted
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 CarMan.Status。")
    }
  }
}