package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.MaintainStatus
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.RepairDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.RepairDto4View
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import tech.simter.reactive.security.ReactiveSecurityService

/**
 * 维修模块接口实现
 *
 * @author sx
 */
@Service
@Transactional
class RepairServiceImpl @Autowired constructor(
  @Value("\${app.register-overdue-hours:24}")
    private val repairOverdueHours: Long,
    private val securityService: ReactiveSecurityService,
    private val repairDao: AccidentDao
) : RepairService{
  override fun update(id: Int, dataDto: RepairDto4FormUpdate): Mono<Void> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun toCheck(id: Int): Mono<Void> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun checked(id: Int, checkInfo: CheckedInfoDto): Mono<Void> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun get(id: Int): Mono<RepairDto4View> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun find(pageNo: Int, pageSize: Int, repairStatuses: List<MaintainStatus>?, search: String?): Mono<Page<RepairDto4View>> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}