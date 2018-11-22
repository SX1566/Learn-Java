package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.bc.dto.MotorcadeDto
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * 事故通用接口 Service 实现。
 *
 * @author jw
 */
@Service
class AccidentCommonServiceImpl @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val bcDao: BcDao
) : AccidentCommonService {
  override fun findMotorcade(includeDisabledStatus: Boolean): Flux<MotorcadeDto> {
    return securityService.verifyHasAnyRole(*ROLES_REPORT_READ, *ROLES_REGISTER_READ)
      .thenMany(bcDao.findMotorcade(includeDisabledStatus))
  }
}
