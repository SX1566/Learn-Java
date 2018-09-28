package cn.gftaxi.traffic.accident.dao

import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.po.AccidentSituation
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import tech.simter.exception.NonUniqueException
import tech.simter.exception.NotFoundException
import java.time.OffsetDateTime

/**
 * 事故案件 Dao。
 *
 * @author RJ
 */
interface AccidentDao {
  /**
   * 验证指定 [事故车号][carPlate] 和 [事发时间][happenTime] 的案件不应存在。
   *
   * 如果存在则返回 [NonUniqueException] 类型的 [Mono.error]，否则返回 [Mono.empty]。
   */
  fun verifyCaseNotExists(carPlate: String, happenTime: OffsetDateTime): Mono<Void>

  /**
   * 根据 [事发时间][happenTime] 生成未使用的事故编号。
   *
   * 生成的事故编号格式为 yyyyMMdd_nn，其中 yyyyMMdd 为 [happenTime] 参数的年月日部分，
   * nn 为两位数字的日流水号，从 01 到 99。
   *
   * @param happenTime 事发时间
   */
  fun nextCaseCode(happenTime: OffsetDateTime): Mono<String>

  /**
   * 根据报案信息创建案件。
   *
   * 1. 生成相应的 [AccidentCase] 和 [AccidentSituation]：
   *    - 自动生成 [案件编号][AccidentCase.code]。
   *    - 设置 [AccidentSituation.stage] = [CaseStage.Drafting]。
   *    - 设置 [AccidentSituation.draftStatus] = [DraftStatus.Drafting]。
   *    - 设置 [AccidentSituation.registerStatus] = [AuditStatus.ToSubmit]。
   * 2. 根据 [事故车号][AccidentCase.carPlate] 自动识别车辆相关信息字段，并自动生成一条自车类型的 [当事车辆][AccidentCar] 信息。
   * 3. 根据 [当事司机姓名][AccidentCase.driverName] 自动识别司机相关信息字段，并自动生成一条自车类型的 [当事人][AccidentPeople] 信息。
   * 4. 返回生成的 [AccidentCase] 和 [AccidentSituation]。
   *
   * @param[caseData] 上报案件的数据
   */
  fun createCase(caseData: AccidentDraftDto4Form): Mono<Pair<AccidentCase, AccidentSituation>>

  /**
   * 获取指定 [主键][id] 的事故案件信息。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getCaseSituation(id: Int): Mono<Pair<AccidentCase, AccidentSituation>>

  /**
   * 获取指定 [主键][id] 的事故案件信息。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getCase(id: Int): Mono<AccidentCase>

  /**
   * 获取指定 [主键][id] 的事故案件信息。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getSituation(id: Int): Mono<AccidentSituation>

  /**
   * 更新指定 [主键][id] 的案件信息。
   *
   * 1. 如果案件不存在，则返回 [NotFoundException] 类型的 [Mono.error]。
   * 2. 按需生成更新日志。
   *
   * ## 对于当事车辆（cars）、当事人（peoples）、其他物体（others）这 3 个子列表信息的更新说明如下：
   * 1. 如果 [data] 中没有相应的 key，代表无需处理。
   * 2. 如果 [data] 中有相应的 key，且其值为 size=0 的 [List]，代表要清空相应的子列表信息。
   * 3. 如果 [data] 中有相应的 key，且其值为 size>0 的 [List]，[List] 内的元素用 item 代表时：
   *   - 【增】如果 item 没有 id，代表为新增一个元素，新增元素的属性值都在 item.data 内。
   *   - 【改】如果 item 有 id，代表更新现有元素的其它属性值，要更新的属性值都在 item.data 内；
   *           此时若 item.data 内没有除 id 外的其它属性数据，则代表此元素无需任何修改，直接忽略无需处理。
   *   - 【删】系统中现有的子列表元素多出的部分就是代表此次要删除的元素。
   *
   * ## 更新当事车辆信息的例子：
   *
   * 假设系统中现有 id=1、2 两条当事车辆信息，传入的 [data] 参数中 [data]&lceil;"cars"&rfloor; 的值为
   * `[{sn: 1, carPlate="xxx"}, {id:1, sn: 2, model: "小车"}]`。这代表：
   * 1. {sn: 1, carPlate="xxx"} 为新增的数据，updatedTime 设为当前时间。
   * 2. 更新 id=1 的现有数据的 sn=2、model="小车"，updatedTime 更新为当前时间，其余属性不更新。
   * 3. 删除 id=2 的现有数据。
   *
   * @param[data] 要更新的数据，数据的键值限制在 [AccidentDraftDto4FormUpdate] 和 [AccidentRegisterDto4FormUpdate] 允许的范围内
   * @param[targetType] 更新类型：如 AccidentDraft、AccidentRegister、AccidentReport，默认为 Accident
   * @param[generateLog] 是否生成更新日志，默认为 true
   * @return 更新完毕的 [Mono] 信号
   */
  fun update(id: Int, data: Map<String, Any?>, targetType: String = "Accident", generateLog: Boolean = true): Mono<Void>

  /**
   * 获取指定条件的事故报案分页信息。
   *
   * 1. 模糊搜索 [事故编号][AccidentCase.code]、[事故车号][AccidentCase.carPlate]、[当事司机姓名][AccidentCase.driverName]。
   * 2. 返回结果按 [事发时间][AccidentCase.happenTime] 逆序排序，
   *
   * @param[draftStatuses] -[事故报案状态][AccidentSituation.draftStatus]，为空代表不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   */
  fun findDraft(pageNo: Int = 1, pageSize: Int = 25, draftStatuses: List<DraftStatus>? = null, search: String? = null)
    : Mono<Page<AccidentDraftDto4View>>

  /**
   * 获取指定 [主键][id] 的事故报案信息。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getDraft(id: Int): Mono<AccidentDraftDto4Form>

  /**
   * 获取指定 [主键][id] 事故的报案状态。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getDraftStatus(id: Int): Mono<DraftStatus>

  /**
   * 获取指定条件的事故登记分页信息。
   *
   * 1. 模糊搜索 [事故编号][AccidentCase.code]、[事故车号][AccidentCase.carPlate]、[当事司机姓名][AccidentCase.driverName]。
   * 2. 返回结果按 [事发时间][AccidentCase.happenTime] 逆序排序，
   *
   * @param[registerStatuses] -[事故登记状态][AccidentSituation.registerStatus]，为空代表不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   */
  fun findRegister(pageNo: Int = 1, pageSize: Int = 25, registerStatuses: List<AuditStatus>? = null, search: String? = null)
    : Mono<Page<AccidentRegisterDto4View>>

  /**
   * 获取指定 [主键][id] 的事故登记信息。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getRegister(id: Int): Mono<AccidentRegisterDto4Form>

  /**
   * 获取指定 [主键][id] 事故的登记状态。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getRegisterStatus(id: Int): Mono<AuditStatus>

  /**
   * 获取指定条件的事故报告分页信息。
   *
   * 1. 模糊搜索 [事故编号][AccidentCase.code]、[事故车号][AccidentCase.carPlate]、[当事司机姓名][AccidentCase.driverName]。
   * 2. 返回结果按 [事发时间][AccidentCase.happenTime] 逆序排序，
   *
   * @param[reportStatuses] -[事故报告状态][AccidentSituation.reportStatus]，为空代表不限定
   * @param[search] 模糊搜索的条件值，为空则忽略
   */
  fun findReport(pageNo: Int = 1, pageSize: Int = 25, reportStatuses: List<AuditStatus>? = null, search: String? = null)
    : Mono<Page<AccidentReportDto4View>>

  /**
   * 获取指定 [主键][id] 的事故报告信息。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getReport(id: Int): Mono<AccidentReportDto4Form>

  /**
   * 获取指定 [主键][id] 事故的报告状态。
   *
   * 如果案件不存，则返回 [Mono.empty]。
   */
  fun getReportStatus(id: Int): Mono<AuditStatus>
}