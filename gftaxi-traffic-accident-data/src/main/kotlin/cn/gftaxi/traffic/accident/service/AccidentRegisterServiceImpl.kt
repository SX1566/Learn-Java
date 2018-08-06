package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Form
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.security.SecurityService

/**
 * 事故登记 Service 实现。
 *
 * @author RJ
 */
@Service
@Transactional
class AccidentRegisterServiceImpl @Autowired constructor(
  private val securityService: SecurityService,
  private val accidentRegisterDao: AccidentRegisterDao
) : AccidentRegisterService {
  override fun statSummary(): Flux<AccidentRegisterDto4StatSummary> {
    securityService.verifyHasAnyRole(*READ_ROLES)
    return accidentRegisterDao.statSummary()
  }

  override fun findTodo(status: Status?): Flux<AccidentRegisterDto4Todo> {
    securityService.verifyHasAnyRole(*READ_ROLES)
    return accidentRegisterDao.findTodo(status)
  }

  override fun findChecked(pageNo: Int, pageSize: Int, status: Status?, search: String?)
    : Mono<Page<AccidentRegisterDto4Checked>> {
    securityService.verifyHasAnyRole(*READ_ROLES)
    return accidentRegisterDao.findChecked(pageNo, pageSize, status, search)
  }

  override fun get(id: Int): Mono<AccidentRegisterDto4Form> {
    TODO("not implemented")
  }

  override fun update(id: Int, data: Map<String, Any?>): Mono<Void> {
    TODO("not implemented")
  }

  override fun toCheck(id: Int): Mono<Void> {
    TODO("not implemented")
  }

  override fun checked(id: Int): Mono<Void> {
    TODO("not implemented")
  }
}