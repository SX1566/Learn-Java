package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [AccidentDraft.Status] 的 [AttributeConverter] 实现。
 * 实现报案状态属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class AccidentDraftStatusConverter : AttributeConverter<Status, Short> {
  override fun convertToDatabaseColumn(attribute: Status): Short {
    return attribute.value()
  }

  override fun convertToEntityAttribute(dbData: Short): Status {
    return when (dbData) {
      Status.Todo.value() -> Status.Todo
      Status.Done.value() -> Status.Done
      else -> throw IllegalArgumentException("无法转换值 '$dbData' 为 AccidentDraft.Status。")
    }
  }
}