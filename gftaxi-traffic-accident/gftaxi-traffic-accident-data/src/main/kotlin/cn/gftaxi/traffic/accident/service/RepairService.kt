package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.common.MaintainStatus
import cn.gftaxi.traffic.accident.dto.RepairDto4FormUpdate
import cn.gftaxi.traffic.accident.dto.RepairDto4View
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono

/**
 * 维修模块 Service
 *
 * @author SX
 */
interface RepairService {
  fun find(pageNo: Int = 1, pageSize: Int = 25, repairStatuses: List<MaintainStatus>? = null, search: String? = null)
    : Mono<Page<RepairDto4View>>

  fun get(id: Int): Mono<RepairDto4View>


  fun update(id: Int, dataDto: RepairDto4FormUpdate): Mono<Void>

  fun toCheck(id: Int):Mono<Void>

  fun checked(id: Int,checkInfo: CheckedInfoDto): Mono<Void>




}