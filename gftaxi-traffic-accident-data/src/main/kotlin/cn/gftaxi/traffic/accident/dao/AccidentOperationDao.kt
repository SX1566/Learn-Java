package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import reactor.core.publisher.Mono

/**
 * 事故操作记录 Dao。
 *
 * @author RJ
 */
interface AccidentOperationDao {
  /**
   * 创建一条新的操作记录。
   *
   * 实现者内部需将操作时间设置为当前时间，操作人设置为当前用户的信息。
   */
  fun create(
    operationType: OperationType,
    targetType: TargetType,
    targetId: Int,
    tag: Int = 0,
    comment: String? = null,
    attachmentId: String? = null,
    attachmentName: String? = null
  ): Mono<AccidentOperation>
}