package cn.gftaxi.traffic.accident.dto

import java.time.OffsetDateTime

/**
 * 上报案件信息用 DTO。
 *
 * @author RJ
 */
class AccidentDraftDto4Submit : AccidentDraftDto4Update() {
  var source: String? by data
  var authorName: String? by data
  var authorId: String? by data
  var reportTime: OffsetDateTime? by data
}