package cn.gftaxi.traffic.accident

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.po.AccidentDraft
import cn.gftaxi.traffic.accident.po.AccidentDraft.Status.Todo
import cn.gftaxi.traffic.accident.po.AccidentOperation
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.*
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentRegister
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import cn.gftaxi.traffic.accident.po.AccidentRegister.Status.Draft
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * 构建 PO 实例测试数据的工具类。
 *
 * @author RJ
 */
object POUtils {
  private val logger = LoggerFactory.getLogger(POUtils::class.java)
  private var currentCode: Int = 0

  /** 在指定区间随机生成一个数字 */
  fun randomInt(start: Int, end: Int) = Random().nextInt(end + 1 - start) + start

  /** 获取下一个事故编号 */
  fun nextCode(ymd: String = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))): String {
    return "${ymd}_${++currentCode}"
  }

  private var idMap = hashMapOf<String, Int>()
  /** 获取指定类型的下一个自动 ID 值 */
  fun nextId(type: String = "T"): Int {
    if (!idMap.containsKey(type)) idMap[type] = 1
    else idMap[type] = idMap[type]!! + 1
    return idMap[type]!!
  }

  private var strMap = hashMapOf("粤A." to 100000)
  /** 生成带固定前缀的唯一字符串 */
  fun random(prefix: String = "T"): String {
    if (!strMap.containsKey(prefix)) strMap[prefix] = 1
    else strMap[prefix] = strMap[prefix]!! + 1
    return "$prefix${strMap[prefix]}"
  }

  /** 构建新的事故报案 */
  fun randomAccidentDraft(
    id: Int? = nextId("draft"),
    code: String = nextCode(),
    status: AccidentDraft.Status = Todo,
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(1),
    overdue: Boolean = false
  ): AccidentDraft {
    return AccidentDraft(
      id = id,
      code = code,
      status = status,
      happenTime = happenTime,
      overdueCreate = overdue,
      carPlate = random("粤A."),
      driverName = random("driver"),
      location = random("location"),
      // 超过 12 小时为逾期报案
      createTime = when {
        overdue -> happenTime.plusHours(12 + 2)
        else -> happenTime.plusMinutes(1)
      },
      hitForm = random("hitForm"),
      hitType = random("hitType"),
      source = "BC",
      authorName = random("authorName"),
      authorId = random("authorId")
    )
  }

  /** 构建新的事故登记 */
  fun randomAccidentRegister(
    draft: AccidentDraft,
    status: AccidentRegister.Status = Draft,
    driverType: DriverType = DriverType.Official,
    overdue: Boolean? = null
  ): AccidentRegister {
    return AccidentRegister(
      id = draft.id,
      status = status,

      carPlate = draft.carPlate,
      carId = nextId("car"),
      motorcadeName = random("mc"),

      driverName = draft.driverName,
      driverId = nextId("driver"),
      driverType = driverType,

      happenTime = draft.happenTime,
      location = draft.location,
      gpsSpeed = 30,
      overdueRegister = overdue,
      // 超过 24 小时为逾期登记
      registerTime = when {
        overdue == null -> null
        overdue -> draft.happenTime.plusHours(24 + 2)
        else -> draft.happenTime.plusMinutes(2)
      },

      hitForm = draft.hitForm,
      hitType = draft.hitType
    )
  }

  /** 构建新的事故操作记录 */
  fun randomAccidentOperation(
    operateTime: OffsetDateTime? = null,
    operationType: OperationType,
    targetId: Int,
    targetType: AccidentOperation.TargetType
  ): AccidentOperation {
    return AccidentOperation(
      operatorId = nextId("operator"),
      operatorName = random("operator"),
      operateTime = operateTime ?: OffsetDateTime.now(),
      operationType = operationType,
      targetId = targetId,
      targetType = targetType,
      comment = random("comment"),
      attachmentId = random("attach"),
      attachmentName = random("attachN")
    )
  }

  /** 构建新的事故登记全套记录：含事故报案、事故登记、创建信息、提交信息、审核信息 */
  fun randomAccidentRegisterRecord(
    code: String = nextCode(),
    status: AccidentRegister.Status = Draft,
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(1),
    driverType: DriverType = DriverType.Official,
    overdueReport: Boolean = false,
    overdueRegister: Boolean? = null
  ): Pair<AccidentRegister, Map<OperationType, AccidentOperation>> {
    val operations = hashMapOf<OperationType, AccidentOperation>()
    // 事故报案
    val accidentDraft = randomAccidentDraft(
      code = code,
      status = if (status == Draft) Todo else AccidentDraft.Status.Done,
      happenTime = happenTime,
      overdue = overdueReport
    )

    // 事故登记
    val accidentRegister = randomAccidentRegister(
      draft = accidentDraft,
      status = status,
      driverType = driverType,
      overdue = overdueRegister
    )

    // 事故登记：创建类记录
    val creation = randomAccidentOperation(
      operateTime = accidentDraft.createTime,
      operationType = Creation,
      targetId = accidentRegister.id!!,
      targetType = TargetType.Register
    )
    operations[Creation] = creation

    if (status != Draft) {
      // 事故登记：提交类记录
      val confirmation = randomAccidentOperation(
        operateTime = accidentRegister.registerTime,
        operationType = Confirmation,
        targetId = accidentRegister.id!!,
        targetType = TargetType.Register
      )
      operations[Confirmation] = confirmation

      if (status == AccidentRegister.Status.Rejected) {
        // 审核不通过的操作记录
        val rejection = randomAccidentOperation(
          operateTime = confirmation.operateTime.plusHours(1),
          operationType = Rejection,
          targetId = accidentRegister.id!!,
          targetType = TargetType.Register
        )
        operations[Rejection] = rejection
      }

      if (status == AccidentRegister.Status.Approved) {
        // 审核通过的操作记录
        val approval = randomAccidentOperation(
          operateTime = confirmation.operateTime.plusHours(2),
          operationType = Approval,
          targetId = accidentRegister.id!!,
          targetType = TargetType.Register
        )
        operations[Approval] = approval
      }
    }

    // 返回创建好的信息
    val pair = Pair(accidentRegister, operations)
    if (logger.isDebugEnabled) logger.debug(pair.toString())
    return pair
  }

  /**
   * 各个状态的事故登记信息都初始化 1 条数据。
   *
   * 按 [AccidentRegister.Status] 各个状态定义的顺序创建数据。
   *
   * @param[baseTime] 事发时间的基点
   * @param[positive] 按时间正序还是逆序构建数据
   */
  fun randomAccidentRegisterRecord4EachStatus(
    baseTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(1),
    positive: Boolean = false
  ): Map<AccidentRegister.Status, Pair<AccidentRegister, Map<OperationType, AccidentOperation>>> {
    val accidentRegisters = hashMapOf<AccidentRegister.Status, Pair<AccidentRegister, Map<OperationType, AccidentOperation>>>()
    var minus = 0L
    val dir = if (positive) 1L else -1L

    // 1. 草稿案件 1 宗
    var happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[Draft] = randomAccidentRegisterRecord(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = Draft,
      happenTime = happenTime,
      driverType = DriverType.Official
    )

    // 2. 待审核案件 1 宗
    happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.ToCheck] = randomAccidentRegisterRecord(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.ToCheck,
      happenTime = happenTime,
      driverType = DriverType.Shift,
      overdueRegister = false
    )

    // 3. 审核不通过案件 1 宗
    happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.Rejected] = randomAccidentRegisterRecord(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.Rejected,
      happenTime = happenTime,
      driverType = DriverType.Outside,
      overdueRegister = true
    )

    // 4. 审核通过案件 1 宗
    happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.Approved] = randomAccidentRegisterRecord(
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.Approved,
      happenTime = happenTime,
      driverType = DriverType.Official,
      overdueRegister = false
    )

    return accidentRegisters
  }
}