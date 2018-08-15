package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.po.AccidentOperation
import reactor.core.publisher.Mono

/**
 * 事故操作记录 Dao。
 *
 * @author RJ
 */
interface AccidentOperationDao {
  /**
   * 创建一条新的操作记录。
   */
  fun create(po: AccidentOperation): Mono<AccidentOperation>
}