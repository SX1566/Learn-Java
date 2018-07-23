package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dao.AccidentRegisterDao
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Checked
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4StatSummary
import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Todo
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_CHECK
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_MODIFY
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_READ
import cn.gftaxi.traffic.accident.po.AccidentRegister.Companion.ROLE_SUBMIT
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
    securityService.verifyHasAnyRole(ROLE_READ, ROLE_SUBMIT, ROLE_MODIFY, ROLE_CHECK)
    return accidentRegisterDao.statSummary()
  }

  override fun findTodo(status: AccidentRegister.Status?): Flux<AccidentRegisterDto4Todo> {
    TODO("not implemented")
  }

  override fun findChecked(pageNo: Int, pageSize: Int, status: AccidentRegister.Status?, search: String?): Mono<Page<AccidentRegisterDto4Checked>> {
    TODO("not implemented")
  }
}