package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import reactor.core.publisher.Flux

/**
 * 事故登记 Service。
 *
 * @author RJ
 */
interface AccidentRegisterService {
  /** 按本月、上月、本年的顺序获取事故登记汇总统计信息。 */
  fun statSummary(): Flux<AccidentRegisterDto4StatSummary>
}