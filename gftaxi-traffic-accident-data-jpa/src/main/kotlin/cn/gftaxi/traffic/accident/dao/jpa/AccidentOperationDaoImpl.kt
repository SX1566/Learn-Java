package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.dao.AccidentOperationDao
import cn.gftaxi.traffic.accident.po.AccidentOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * 事故操作记录 Dao 实现。
 *
 * @author RJ
 */
@Component
class AccidentOperationDaoImpl @Autowired constructor(
  @PersistenceContext private val em: EntityManager,
  private val repository: AccidentDraftJpaRepository
) : AccidentOperationDao {
  override fun create(po: AccidentOperation): Mono<AccidentOperation> {
    TODO("not implemented")
  }
}