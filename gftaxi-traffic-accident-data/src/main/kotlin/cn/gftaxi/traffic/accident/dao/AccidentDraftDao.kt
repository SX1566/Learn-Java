package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import java.time.OffsetDateTime

/**
 * 事故报案 Dao。
 *
 * @author RJ
 */
interface AccidentDraftDao {
  /**
   * 获取指定条件的报案分页信息。
   *
   * 返回的列表信息按状态正序+事发时间逆序排序，模糊搜索事故编号、车号、司机。
   *
   * @param[status] 案件状态，为空代表不限定
   * @param[fuzzySearch] 模糊搜索的条件值，为空则忽略
   */
  fun find(pageNo: Int = 1, pageSize: Int = 25, status: Status? = null, fuzzySearch: String? = null)
    : Mono<Page<AccidentDraft>>

  /**
   * 获取所有待登记的报案信息，按事发时间逆序排序。
   */
  fun findTodo(): Flux<AccidentDraft>

  /**
   * 获取指定主键的报案。
   *
   * @return 如果案件不存在则返回 [Mono.empty]
   */
  fun get(id: Int): Mono<AccidentDraft>

  /**
   * 创建新的报案。
   *
   * 如果相同编号或者 "车号和事发时间" 的案件已经存在，
   * 则返回类型为 [NonUniqueException] 的 [Mono.error]
   */
  fun create(po: AccidentDraft): Mono<AccidentDraft>

  /**
   * 更新报案信息。
   *
   * 用于修改车号、司机、事发地点、简要描述、事发时间等。
   *
   * 更新成功返回 `Mono.just(true)`，否则返回 `Mono.just(false)`。
   * 更新成功是指真的更新了某些数据，如果没有修改任何数据则返回 `Mono.just(false)`。
   *
   * @param[id] 要修改案件的 ID
   * @param[data] 要更新的信息，key 为 PO 的属性名，value 为相应的 PO 属性值
   */
  fun update(id: Int, data: Map<String, Any?>): Mono<Boolean>

  /**
   * 根据事发时间生成未使用的事故编号。
   *
   * 生成的事故编号格式为 yyyyMMdd_nn，其中 yyyyMMdd 为 [happenTime] 参数的年月日部分，
   * nn 为两位数字的日流水号，从 01 到 99。
   *
   * @param happenTime 事发时间
   */
  fun nextCode(happenTime: OffsetDateTime): Mono<String>
}