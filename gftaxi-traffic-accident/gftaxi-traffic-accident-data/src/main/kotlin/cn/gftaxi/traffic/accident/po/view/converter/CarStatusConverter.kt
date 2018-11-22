package cn.gftaxi.traffic.accident.po.view.converter

import cn.gftaxi.traffic.accident.po.view.Car
import cn.gftaxi.traffic.accident.po.view.Car.Status
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [Car.Status] 的 [AttributeConverter] 实现。
 * 实现车辆状态属性值与数据库持久化值之间的互转。
 *
 * @author JF
 */
@Converter(autoApply = true)
class CarStatusConverter : AttributeConverter<Status, Short> {
  override fun convertToDatabaseColumn(attribute: Status): Short {
    return attribute.value()
  }

  override fun convertToEntityAttribute(dbData: Short): Status {
    return when (dbData) {
      Status.NewBuy.value() -> Status.NewBuy
      Status.Draft.value() -> Status.Draft
      Status.Enabled.value() -> Status.Enabled
      Status.Disabled.value() -> Status.Disabled
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 Car.Status。")
    }
  }
}