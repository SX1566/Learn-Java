package cn.gftaxi.traffic.accident.rest.webflux

import cn.gftaxi.traffic.accident.po.AccidentDraft
import java.time.OffsetDateTime

/**
 * @author RJ
 */
object TestUtils {
  private var strMap = hashMapOf("粤A." to 100000)
  /** 生成带固定前缀的唯一字符串 */
  fun random(prefix: String = "T"): String {
    if (!strMap.containsKey(prefix)) strMap[prefix] = 1
    else strMap[prefix] = strMap[prefix]!! + 1
    return "$prefix${strMap[prefix]}"
  }

  fun randomAccidentDraft(id: Int? = null, code: String): AccidentDraft {
    return AccidentDraft(
      id = id,
      code = code,
      status = AccidentDraft.Status.Todo,
      carPlate = random("car"),
      driverName = random("driver"),
      happenTime = OffsetDateTime.now(),
      createTime = OffsetDateTime.now(),
      location = random("location"),
      hitForm = random("hitForm"),
      hitType = random("hitType"),
      overdueCreate = false,
      source = random("source"),
      authorName = random("authorName"),
      authorId = random("authorId)")
    )
  }
}