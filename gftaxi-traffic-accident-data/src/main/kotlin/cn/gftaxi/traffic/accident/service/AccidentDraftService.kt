package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Modify
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 事故报案 Service。
 *
 * @author RJ
 */
interface AccidentDraftService {
  /**
   * 获取指定条件的报案分页信息。
   *
   * 返回的列表信息按报案时间逆序排序，模糊搜索事故编号、车号、司机。
   *
   * @param[status] 案件状态，为空代表不限定
   * @param[fuzzySearch] 模糊搜索的条件值，为空则忽略
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_READ] 查询报案信息权限
   */
  fun find(pageNo: Int, pageSize: Int, status: Status?, fuzzySearch: String?): Mono<Page<AccidentDraft>>

  /**
   * 获取所有待登记的报案信息，按报案时间逆序排序。
   *
   * @throws [SecurityException] 无查询报案信息权限
   */
  fun findTodo(): Flux<AccidentDraft>

  /**
   * 获取指定编号的报案。
   *
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_READ] 查询报案信息权限
   */
  fun get(code: String): Mono<AccidentDraft>

  /**
   * 提交新的报案。
   *
   * @return 自动生成的事故编号
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_SUBMIT] 提交报案信息权限
   * @throws [IllegalArgumentException] 指定车号和事发时间的案件已经存在
   */
  fun submit(dto: AccidentDraftDto4Submit): Mono<String>

  /**
   * 修改报案信息。
   *
   * @param[code] 要修改案件的编号
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_MODIFY] 修改报案信息权限
   * @throws [IllegalArgumentException] 指定的案件编号不存在
   */
  fun modify(code: String, dto: AccidentDraftDto4Modify): Mono<Void>
}