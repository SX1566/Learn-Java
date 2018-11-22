package cn.gftaxi.traffic.accident.dto

import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import javax.persistence.MappedSuperclass

/**
 * 上报案件信息用 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentDraftDto4FormSubmit : AccidentDraftDto4FormUpdate() {
  var source: String? by holder
  var authorName: String? by holder
  var authorId: String? by holder

  /** 转换 [AccidentDraftDto4FormSubmit] 为 [Pair]<[AccidentCase], [AccidentSituation]>。 */
  fun toCaseSituation(): Pair<AccidentCase, AccidentSituation> = Pair(
    AccidentCase().also { case ->
      // 复制属性
      case.carPlate = carPlate
      case.driverName = driverName
      case.happenTime = happenTime
      case.location = location
      hitForm?.run { case.hitForm = hitForm }
      hitType?.run { case.hitType = hitType }
      describe?.run { case.describe = describe }
    },
    AccidentSituation().also { situation ->
      // 复制属性
      source?.run { situation.source = source }
      authorName?.run { situation.authorName = authorName }
      authorId?.run { situation.authorId = authorId }
    }
  )
}
