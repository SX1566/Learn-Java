package cn.gftaxi.traffic.accident.rest.webflux

import cn.gftaxi.traffic.accident.po.AccidentDraft
import java.time.OffsetDateTime

/**
 * @author RJ
 */
object TestUtils {
  fun randomAccidentDraft(id: Int? = null, code: String): AccidentDraft {
    return AccidentDraft(
      id = id,
      code = code,
      status = AccidentDraft.Status.Todo,
      carPlate = "car",
      driverName = "driver",
      happenTime = OffsetDateTime.now(),
      createTime = OffsetDateTime.now(),
      location = "location",
      hitForm = "hitForm",
      hitType = "hitType",
      overdueCreate = false,
      source = "source",
      authorName = "authorName",
      authorId = "authorId"
    )
  }
}