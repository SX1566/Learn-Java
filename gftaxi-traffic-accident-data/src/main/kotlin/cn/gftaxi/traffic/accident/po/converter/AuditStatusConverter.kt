package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.po.AccidentSituation
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [AuditStatus] 的 [AttributeConverter] 实现。
 * 实现 [审查状态][AccidentSituation.registerStatus] 属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class AuditStatusConverter : AttributeConverter<AuditStatus?, Short?> {
  override fun convertToDatabaseColumn(attribute: AuditStatus?): Short? {
    return attribute?.value()
  }

  override fun convertToEntityAttribute(dbData: Short?): AuditStatus? {
    return dbData?.run {
      AuditStatus.values().find { it.value() == dbData }
        ?: throw IllegalArgumentException("无法转换值 '$dbData' 为 ${AuditStatus::class.simpleName}。")
    }
  }
}