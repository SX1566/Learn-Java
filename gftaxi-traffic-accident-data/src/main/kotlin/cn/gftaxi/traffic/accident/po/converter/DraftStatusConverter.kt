package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.po.AccidentSituation
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [DraftStatus] 的 [AttributeConverter] 实现。
 * 实现 [报案状态][AccidentSituation.draftStatus] 属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class DraftStatusConverter : AttributeConverter<DraftStatus?, Short?> {
  override fun convertToDatabaseColumn(attribute: DraftStatus?): Short? {
    return attribute?.value()
  }

  override fun convertToEntityAttribute(dbData: Short?): DraftStatus? {
    return dbData?.run {
      DraftStatus.values().find { it.value() == dbData }
        ?: throw IllegalArgumentException("无法转换值 '$dbData' 为 ${DraftStatus::class.simpleName}。")
    }
  }
}