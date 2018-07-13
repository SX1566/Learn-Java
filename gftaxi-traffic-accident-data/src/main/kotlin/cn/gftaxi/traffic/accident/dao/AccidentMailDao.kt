package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import reactor.core.publisher.Flux

/**
 * 事故邮件 Dao。
 *
 * @author RJ
 */
interface AccidentMailDao {
  /**
   * 收取报案邮件。
   *
   * @return 从邮件解析生成的待提交报案信息
   */
  fun receiveMail(): Flux<AccidentDraftDto4Submit>
}