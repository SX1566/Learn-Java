package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.AccidentReportDto4View
import cn.gftaxi.traffic.accident.po.AccidentReport.Companion.READ_ROLES
import cn.gftaxi.traffic.accident.po.AccidentReport.Status
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import tech.simter.exception.PermissionDeniedException

/**
 * 事故报告 Service。
 *
 * @author RJ
 * @author zh
 */
interface AccidentReportService {
  /**
   * 获取指定状态的案件分页信息。
   *
   * 返回的信息按事发时间逆序排序，模糊搜索车队、车号、司机、编号。
   *
   * @param[statuses] 案件状态，为 null 则不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   * @throws [PermissionDeniedException] 不是 [READ_ROLES] 中的任一角色之一
   */
  fun find(pageNo: Int = 1, pageSize: Int = 25, statuses: List<Status>? = null, search: String? = null)
    : Mono<Page<AccidentReportDto4View>>
}