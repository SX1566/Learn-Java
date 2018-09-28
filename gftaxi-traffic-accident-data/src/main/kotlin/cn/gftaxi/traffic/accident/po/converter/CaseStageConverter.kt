package cn.gftaxi.traffic.accident.po.converter

import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.po.AccidentSituation
import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * 一个 [CaseStage] 的 [AttributeConverter] 实现。
 * 实现 [案件阶段标记][AccidentSituation.stage] 属性值与数据库持久化值之间的互转。
 *
 * @author RJ
 */
@Converter(autoApply = true)
class CaseStageConverter : AttributeConverter<CaseStage?, Short?> {
  override fun convertToDatabaseColumn(attribute: CaseStage?): Short? {
    return attribute?.value()
  }

  override fun convertToEntityAttribute(dbData: Short?): CaseStage? {
    return dbData?.run {
      CaseStage.values().find { it.value() == dbData }
        ?: throw IllegalArgumentException("无法转换值 '$dbData' 为 ${CaseStage::class.simpleName}。")
    }
  }
}