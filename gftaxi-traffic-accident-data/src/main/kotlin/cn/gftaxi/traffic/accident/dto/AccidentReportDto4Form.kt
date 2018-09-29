package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.po.*
import cn.gftaxi.traffic.accident.po.converter.AuditStatusConverter
import java.time.OffsetDateTime
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * 事故报告信息表单用 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
class AccidentReportDto4Form : AccidentRegisterDto4Form() {
  //== 报告相关 ==
  /** 报告时间，等于首次将报告信息提交审核的时间 */
  var reportTime: OffsetDateTime? by holder
  /** 是否逾期报告 */
  var overdueReport: Boolean? by holder
  /** 报告信息的处理状态 */
  @get:Convert(converter = AuditStatusConverter::class)
  var reportStatus: AuditStatus? by holder

  companion object {
    private val dtoProperties = AccidentReportDto4Form::class.memberProperties
      .filterIsInstance<KMutableProperty<*>>()
      .associate { it.name to it }

    /** 转换 [AccidentCase]、[AccidentSituation] 为 [AccidentReportDto4Form] */
    @Suppress("UNCHECKED_CAST")
    fun from(case: AccidentCase, situation: AccidentSituation): AccidentReportDto4Form {
      return AccidentReportDto4Form().also { dto ->
        // 复制 AccidentCase 的属性
        AccidentCase::class.memberProperties.forEach { p ->
          dtoProperties[p.name]?.setter?.let { setter ->
            var value: Any? = p.get(case)

            // 几个特殊属性的转换
            if (p.name == "cars")
              value = (value as List<AccidentCar>?)?.run { map { AccidentCarDto4Form.from(it) } }
            if (p.name == "peoples")
              value = (value as List<AccidentPeople>?)?.run { map { AccidentPeopleDto4Form.from(it) } }
            if (p.name == "others")
              value = (value as List<AccidentOther>?)?.run { map { AccidentOtherDto4Form.from(it) } }
            setter.call(dto, value)
          }
        }

        // 复制 AccidentSituation 的属性
        AccidentSituation::class.memberProperties.forEach { p ->
          dtoProperties[p.name]?.setter?.call(dto, p.get(situation))
        }

        // 复制不同名的属性值
        dto.checkedCount = situation.registerCheckedCount
        dto.checkedComment = situation.registerCheckedComment
        dto.checkedAttachments = situation.registerCheckedAttachments
      }
    }

    fun from(pair: Pair<AccidentCase, AccidentSituation>): AccidentReportDto4Form {
      return from(pair.first, pair.second)
    }
  }
}