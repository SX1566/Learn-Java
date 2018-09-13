package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Update
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import tech.simter.exception.NotFoundException

/**
 * 事故报案 Service。
 *
 * @author RJ
 */
interface AccidentDraftService {
  /**
   * 获取指定条件的报案分页信息。
   *
   * 返回的列表信息按按状态正序+事发时间逆序排序，模糊搜索事故编号、车号、司机。
   *
   * @param[status] 案件状态，为空代表不限定
   * @param[fuzzySearch] 模糊搜索的条件值，为空则忽略
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_READ] 查询报案信息权限
   */
  fun find(pageNo: Int, pageSize: Int, status: Status?, fuzzySearch: String?): Mono<Page<AccidentDraft>>

  /**
   * 获取所有待登记的报案信息，按事发时间逆序排序。
   *
   * @throws [SecurityException] 无查询报案信息权限
   */
  fun findTodo(): Flux<AccidentDraft>

  /**
   * 获取指定主键的报案。
   *
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_READ] 查询报案信息权限
   * @return 如果案件不存在则返回 [Mono.empty]
   */
  fun get(id: Int): Mono<AccidentDraft>

  /**
   * 上报新的报案。
   *
   * @return 自动生成的事故 ID、编号
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_SUBMIT] 上报案件信息权限
   * @throws [NonUniqueException] 指定车号和事发时间的案件已经存在
   */
  fun submit(dto: AccidentDraftDto4Submit): Mono<Pair<Int, String>>

  /**
   * 修改报案信息。
   *
   * @param[id] 要修改案件的 ID
   * @param[data] 要更新的信息，key 为 [AccidentDraftDto4Update] 属性名，value 为该 DTO 相应的属性值，
   *              使用者只传入已改动的属性键值对，没有改动的属性不要传入来。
   * @throws [SecurityException] 无 [AccidentDraft.ROLE_MODIFY] 修改报案信息权限
   * @throws [NotFoundException] 指定的案件编号不存在
   */
  fun update(id: Int, data: Map<String, Any?>): Mono<Void>
}