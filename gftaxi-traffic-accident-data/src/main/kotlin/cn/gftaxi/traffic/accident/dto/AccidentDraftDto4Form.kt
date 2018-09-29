package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import cn.gftaxi.traffic.accident.po.converter.DraftStatusConverter
import cn.gftaxi.traffic.accident.po.converter.DriverTypeConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * 上报案件信息用 DTO。
 *
 * @author RJ
 */
@Entity
class AccidentDraftDto4Form : AccidentDraftDto4FormUpdate() {
  @get:Id
  var id: Int? by holder
  var code: String? by holder
  var motorcadeName: String? by holder
  /** 报案状态 */
  @get:Convert(converter = DraftStatusConverter::class)
  var draftStatus: DraftStatus? by holder
  @get:Convert(converter = DriverTypeConverter::class)
  var driverType: DriverType? by holder

  var source: String? by holder
  var authorName: String? by holder
  var authorId: String? by holder
  var draftTime: OffsetDateTime? by holder
  var overdueDraft: Boolean? by holder

  companion object {
    private val dtoProperties = AccidentDraftDto4Form::class.memberProperties
      .filterIsInstance<KMutableProperty<*>>()
      .associate { it.name to it }

    /** 转换 [AccidentCase]、[AccidentSituation] 为 [AccidentDraftDto4Form] */
    @Suppress("UNCHECKED_CAST")
    fun from(case: AccidentCase, situation: AccidentSituation): AccidentDraftDto4Form {
      return AccidentDraftDto4Form().also { dto ->
        // 复制 AccidentCase 的属性
        AccidentCase::class.memberProperties.forEach { p ->
          dtoProperties[p.name]?.setter?.call(dto, p.get(case))
        }

        // 复制 AccidentSituation 的属性
        AccidentSituation::class.memberProperties.forEach { p ->
          dtoProperties[p.name]?.setter?.call(dto, p.get(situation))
        }
      }
    }

    fun from(pair: Pair<AccidentCase, AccidentSituation>): AccidentDraftDto4Form {
      return from(pair.first, pair.second)
    }
  }
}