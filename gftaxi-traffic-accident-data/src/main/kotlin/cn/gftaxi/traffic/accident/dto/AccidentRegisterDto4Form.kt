package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.po.*
import cn.gftaxi.traffic.accident.po.converter.AuditStatusConverter
import cn.gftaxi.traffic.accident.po.converter.CaseStageConverter
import cn.gftaxi.traffic.accident.po.converter.DraftStatusConverter
import tech.simter.operation.po.Attachment
import tech.simter.operation.po.converter.AttachmentsConverter
import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * 事故登记信息表单用 DTO。
 *
 * @author RJ
 */
@Entity
@MappedSuperclass
open class AccidentRegisterDto4Form : AccidentRegisterDto4FormUpdate() {
  /** 案件主体状态 */
  @get:Convert(converter = CaseStageConverter::class)
  var stage: CaseStage? by holder

  //== 报案相关 ==
  /** 报案来源：BC-BC系统Web端、MAIL-邮件、WEIXIN-微信、SMS-短信、{appId}-应用ID */
  @get:Column(length = 10, nullable = false)
  var source: String? by holder
  /** 报案时间 */
  var draftTime: OffsetDateTime? by holder
  /** 是否逾期报案 */
  var overdueDraft: Boolean? by holder
  /** 接案人姓名 */
  @get:Column(length = 50)
  var authorName: String? by holder
  /** 接案人标识：邮件报案为邮箱、短信报案为手机号、其余为对应的登陆账号 */
  @get:Column(length = 50)
  var authorId: String? by holder
  /** 报案信息的处理状态 */
  @get:Convert(converter = DraftStatusConverter::class)
  var draftStatus: DraftStatus? by holder

  //== 登记相关 ==
  /** 登记时间，等于首次将登记信息提交审核的时间 */
  var registerTime: OffsetDateTime? by holder
  /** 是否逾期登记 */
  var overdueRegister: Boolean? by holder
  /** 登记信息的处理状态 */
  @get:Convert(converter = AuditStatusConverter::class)
  var registerStatus: AuditStatus? by holder
  /** 登记信息的审核次数 */
  var checkedCount: Int? by holder
  /** 登记信息最后一次审核的审核意见 */
  var checkedComment: String? by holder

  /** 登记信息最后一次审核的审核附件 */
  @get:Convert(converter = AttachmentsConverter::class)
  var checkedAttachments: List<Attachment>? by holder

  companion object {
    private val dtoProperties = AccidentRegisterDto4Form::class.memberProperties
      .filterIsInstance<KMutableProperty<*>>()
      .associate { it.name to it }

    /** 转换 [AccidentCase]、[AccidentSituation] 为 [AccidentRegisterDto4Form] */
    @Suppress("UNCHECKED_CAST")
    fun from(case: AccidentCase, situation: AccidentSituation): AccidentRegisterDto4Form {
      return AccidentRegisterDto4Form().also { dto ->
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

    fun from(pair: Pair<AccidentCase, AccidentSituation>): AccidentRegisterDto4Form {
      return from(pair.first, pair.second)
    }
  }
}