package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.dao.jpa.repository.AccidentOperationJpaRepository
import cn.gftaxi.traffic.accident.po.AccidentOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * 事故操作记录 Dao 实现。
 *
 * @author RJ
 */
@Component
class AccidentOperationDaoImpl @Autowired constructor(
  private val repository: AccidentOperationJpaRepository
) : AccidentOperationDao {
  override fun create(po: AccidentOperation): Mono<AccidentOperation> {
    return Mono.just(repository.save(po))
  }
}