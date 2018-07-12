package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.po.AccidentDraft
import javafx.animation.Animation.Status
import org.springframework.data.domain.Page
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 事故报案 Dao。
 *
 * @author RJ
 */
interface AccidentDraftDao {
  /**
   * 获取指定条件的报案分页信息。
   *
   * 返回的列表信息按报案时间逆序排序，模糊搜索事故编号、车号、司机。
   *
   * @param[status] 案件状态，为空代表不限定
   * @param[fuzzySearch] 模糊搜索的条件值
   */
  fun find(pageNo: Int, pageSize: Int, status: Status, fuzzySearch: String): Flux<Page<AccidentDraft>>

  /**
   * 获取所有待登记的报案信息，按报案时间逆序排序。
   */
  fun findTodo(): Flux<AccidentDraft>

  /**
   * 获取指定编号的报案。
   */
  fun get(code: String): Mono<AccidentDraft>

  /**
   * 创建新的报案。
   *
   * @throws [IllegalArgumentException] 指定车号和事发时间的案件已经存在
   */
  fun create(po: AccidentDraft): Mono<Void>

  /**
   * 更新报案信息。
   *
   * 用于修改车号、司机、事发地点、简要描述、事发时间等。
   *
   * @param[code] 要修改案件的编号
   * @param[data] 要更新的信息，key 为 PO 的属性名，value 为相应的 PO 属性值
   * @return 更新成功返回 true，否则返回 false
   */
  fun update(code: String, data: Map<String, Object>): Mono<Boolean>
}