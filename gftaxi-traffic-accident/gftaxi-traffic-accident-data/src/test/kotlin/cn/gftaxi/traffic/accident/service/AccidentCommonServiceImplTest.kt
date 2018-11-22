package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.bc.dto.MotorcadeDto
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REGISTER_READ
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLES_REPORT_READ
import cn.gftaxi.traffic.accident.common.MotorcadeStatus
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import kotlin.test.assertEquals

/**
 * 事故通用接口 Service 实现。
 *
 * @author jw
 */
@SpringJUnitConfig(AccidentCommonServiceImpl::class)
@MockBean(BcDao::class, ReactiveSecurityService::class)
class AccidentCommonServiceImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentCommonService: AccidentCommonService,
  private val bcDao: BcDao
) {
  @Test
  fun find() {
    // mock
    val includeDisabled = true
    val expect = arrayListOf<MotorcadeDto>().apply {
      for (i in 0..5) add(MotorcadeDto(id = i, name = "一分${i + 1}队", sn = i, status = MotorcadeStatus.Enabled,
        branchId = 1, branchName = "一分公司", captainId = Math.random().toInt(), captainName = "captain#$i")
      )
    }
    `when`(securityService.verifyHasAnyRole(*ROLES_REPORT_READ, *ROLES_REGISTER_READ)).thenReturn(Mono.empty())
    `when`(bcDao.findMotorcade(includeDisabled)).thenReturn(expect.toFlux())

    // invoke and verify
    StepVerifier.create(accidentCommonService.findMotorcade(includeDisabled).collectList())
      .consumeNextWith {
        for (i in 0 until it.size) assertEquals(it[i], expect[i])
      }

    verify(securityService).verifyHasAnyRole(*ROLES_REPORT_READ, *ROLES_REGISTER_READ)
    verify(bcDao).findMotorcade(includeDisabled)
  }
}