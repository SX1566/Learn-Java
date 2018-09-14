package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dto.CaseRelatedInfoDto
import cn.gftaxi.traffic.accident.po.*
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType
import cn.gftaxi.traffic.accident.po.AccidentOperation.OperationType.*
import cn.gftaxi.traffic.accident.po.AccidentOperation.TargetType
import cn.gftaxi.traffic.accident.po.AccidentPeople.Sex.Male
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.EntityManager

/**
 * 单元测试工具类。
 *
 * @author RJ
 */
object POUtils {
  private val logger = LoggerFactory.getLogger(POUtils::class.java)
  private var currentCode: Int = 0

  /** 在指定区间随机生成一个数字 */
  fun randomInt(start: Int, end: Int) = Random().nextInt(end + 1 - start) + start

  /** 获取下一个事故编号 */
  fun nextCode(ymd: String): String {
    currentCode = when (currentCode) {
      99 -> 0
      else -> currentCode
    }
    return "${ymd}_${++currentCode}"
  }

  private var idMap = hashMapOf<String, Int>()
  /** 获取指定类型的下一个自动 ID 值 */
  fun nextId(type: String): Int {
    if (!idMap.containsKey(type)) idMap[type] = 1
    else idMap[type] = idMap[type]!! + 1
    return idMap[type]!!
  }

  private var strMap = hashMapOf("粤A." to 100000)
  /** 生成带固定前缀的唯一字符串 */
  fun random(prefix: String): String {
    if (!strMap.containsKey(prefix)) strMap[prefix] = 1
    else strMap[prefix] = strMap[prefix]!! + 1
    return "$prefix${strMap[prefix]}"
  }

  /** 构建新的事故报案 */
  fun randomAccidentDraft(
    code: String,
    status: AccidentDraft.Status,
    happenTime: OffsetDateTime,
    overdueDraft: Boolean = false,
    carPlate: String? = null,
    driverName: String? = null
  ): AccidentDraft {
    return AccidentDraft(
      code = code,
      status = status,
      happenTime = happenTime,
      overdueDraft = overdueDraft,
      carPlate = carPlate ?: random("粤A."),
      driverName = driverName ?: random("driver"),
      location = random("location"),
      // 超过 12 小时为逾期报案
      draftTime = when {
        overdueDraft -> happenTime.plusHours(12 + 2)
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
    status: AccidentRegister.Status,
    driverType: DriverType,
    overdueRegister: Boolean? = null,
    registerTime: OffsetDateTime? = null,
    cars: Set<AccidentCar>? = null,
    peoples: Set<AccidentPeople>? = null,
    others: Set<AccidentOther>? = null
  ): AccidentRegister {
    val accidentRegister = AccidentRegister(
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
      overdueRegister = overdueRegister,
      // 超过 24 小时为逾期登记
      registerTime = (registerTime ?: when {
        overdueRegister == null -> null
        overdueRegister -> draft.happenTime.plusHours(24 + 2)
        else -> draft.happenTime.plusMinutes(2)
      }),

      hitForm = draft.hitForm,
      hitType = draft.hitType
    )
    accidentRegister.cars = cars
    accidentRegister.peoples = peoples
    accidentRegister.others = others
    return accidentRegister
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
  private fun randomAccidentRegisterRecord(
    em: EntityManager,
    code: String,
    status: AccidentRegister.Status,
    happenTime: OffsetDateTime = OffsetDateTime.now(),
    driverType: DriverType,
    overdueDraft: Boolean = false,
    overdueRegister: Boolean? = null
  ): Triple<AccidentRegister, AccidentDraft, Map<OperationType, AccidentOperation>> {
    val operations = hashMapOf<OperationType, AccidentOperation>()
    // 事故报案
    val accidentDraft = randomAccidentDraft(
      code = code,
      status = if (status == AccidentRegister.Status.Draft) AccidentDraft.Status.Todo else AccidentDraft.Status.Done,
      happenTime = happenTime,
      overdueDraft = overdueDraft
    )
    em.persist(accidentDraft)

    // 事故登记
    val accidentRegister = randomAccidentRegister(
      draft = accidentDraft,
      status = status,
      driverType = driverType,
      overdueRegister = overdueRegister
    )
    em.persist(accidentRegister)

    // 事故登记：创建类记录
    val creation = randomAccidentOperation(
      operateTime = accidentDraft.draftTime,
      operationType = Creation,
      targetId = accidentRegister.id!!,
      targetType = TargetType.Register
    )
    em.persist(creation)
    operations[Creation] = creation

    if (status != AccidentRegister.Status.Draft) {
      // 事故登记：提交类记录
      val confirmation = randomAccidentOperation(
        operateTime = accidentRegister.registerTime,
        operationType = Confirmation,
        targetId = accidentRegister.id!!,
        targetType = TargetType.Register
      )
      em.persist(confirmation)
      operations[Confirmation] = confirmation

      if (status == AccidentRegister.Status.Rejected) {
        // 审核不通过的操作记录
        val rejection = randomAccidentOperation(
          operateTime = confirmation.operateTime.plusHours(1),
          operationType = Rejection,
          targetId = accidentRegister.id!!,
          targetType = TargetType.Register
        )
        em.persist(rejection)
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
        em.persist(approval)
        operations[Approval] = approval
      }
    }

    // 返回创建好的信息
    val triple = Triple(accidentRegister, accidentDraft, operations)
    if (logger.isDebugEnabled) logger.debug(triple.toString())
    return triple
  }

  /**
   * 各个状态的事故登记信息都初始化 1 条数据。
   *
   * 按 [AccidentRegister.Status] 各个状态定义的顺序创建数据。
   *
   * @param[baseTime] 事发时间的基点
   * @param[positive] 按时间正序还是逆序构建数据
   */
  fun randomAccidentRegisterRecord4EachStatus(em: EntityManager, baseTime: OffsetDateTime, positive: Boolean)
    : Map<AccidentRegister.Status, Triple<AccidentRegister, AccidentDraft, Map<OperationType, AccidentOperation>>> {
    val accidentRegisters = hashMapOf<AccidentRegister.Status, Triple<AccidentRegister, AccidentDraft,
      Map<OperationType, AccidentOperation>>>()
    var minus = 0L
    val dir = if (positive) 1L else -1L

    // 1. 草稿案件 1 宗
    var happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.Draft] = randomAccidentRegisterRecord(
      em = em,
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.Draft,
      happenTime = happenTime,
      driverType = DriverType.Official
    )

    // 2. 待审核案件 1 宗
    happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.ToCheck] = randomAccidentRegisterRecord(
      em = em,
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.ToCheck,
      happenTime = happenTime,
      driverType = DriverType.Shift,
      overdueRegister = false
    )

    // 3. 审核不通过案件 1 宗
    happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.Rejected] = randomAccidentRegisterRecord(
      em = em,
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.Rejected,
      happenTime = happenTime,
      driverType = DriverType.Outside,
      overdueRegister = true
    )

    // 4. 审核通过案件 1 宗
    happenTime = baseTime.plusHours((++minus) * dir)
    accidentRegisters[AccidentRegister.Status.Approved] = randomAccidentRegisterRecord(
      em = em,
      code = nextCode(happenTime.format(FORMAT_TO_YYYYMMDD)),
      status = AccidentRegister.Status.Approved,
      happenTime = happenTime,
      driverType = DriverType.Official,
      overdueRegister = false
    )

    return accidentRegisters
  }

  fun randomCaseRelatedInfoDto(): CaseRelatedInfoDto {
    val now = OffsetDateTime.now()
    return CaseRelatedInfoDto(
      // 事发时的信息
      motorcadeName = random("mc"),
      contractType = random("contractType"),
      contractDrivers = random("contractDrivers"),

      // 当事车辆信息
      carId = nextId("car"),
      carModel = random("carModel"),
      carOperateDate = now.minusYears(3).toLocalDate(),

      // 当事司机信息
      driverId = nextId("driver"),
      driverUid = random("driverUid"),
      driverType = DriverType.Official,
      driverPhone = random("driverPhone"),
      driverHiredDate = now.minusYears(2).toLocalDate(),
      driverBirthDate = now.minusYears(30).toLocalDate(),
      driverIdentityCode = random("driverIdentityCode"),
      driverServiceCode = random("driverServiceCode"),
      driverOrigin = random("driverOrigin"),
      driverLicenseDate = now.minusYears(10).toLocalDate(),
      relatedDriverName = random("relatedDriverName"),
      relatedDriverPhone = random("relatedDriverPhone"),

      // 历史统计
      historyAccidentCount = randomInt(0, 10).toShort(),
      historyTrafficOffenceCount = randomInt(0, 10).toShort(),
      historyServiceOffenceCount = randomInt(0, 10).toShort(),
      historyComplainCount = randomInt(0, 10).toShort()
    )
  }

  /** 构建新的当事车辆 */
  fun randomAccidentCar(
    parent: AccidentRegister,
    sn: Short = 0,
    name: String = random("name"),
    type: String = random("type"),
    model: String = random("model")
  ): AccidentCar {
    return AccidentCar(
      parent = parent,
      sn = sn,
      name = name,
      type = type,
      model = model,
      towCount = randomInt(1, 10).toShort(),
      repairType = random("repairType"),
      guessTowMoney = BigDecimal("${randomInt(1, 100)}.00"),
      guessRepairMoney = BigDecimal("${randomInt(1, 100)}.00"),
      actualTowMoney = BigDecimal("${randomInt(1, 100)}.00"),
      actualRepairMoney = BigDecimal("${randomInt(1, 100)}.00"),
      damageState = random("damageState"),
      followType = random("followType"),
      updatedTime = OffsetDateTime.now()
    )
  }

  /** 构建新的当事人 */
  fun randomAccidentPeople(
    parent: AccidentRegister,
    sn: Short = 0,
    name: String = random("name"),
    type: String = random("type"),
    phone: String = random("phone")
  ): AccidentPeople {
    return AccidentPeople(
      parent = parent,
      sn = sn,
      name = name,
      type = type,
      sex = Male,
      phone = phone,
      transportType = random("transportType"),
      duty = random("duty"),
      damageState = random("damageState"),
      damageMoney = BigDecimal("${randomInt(1, 100)}.00"),
      treatmentMoney = BigDecimal("${randomInt(1, 100)}.00"),
      compensateMoney = BigDecimal("${randomInt(1, 100)}.00"),
      followType = random("followType"),
      updatedTime = OffsetDateTime.now()
    )
  }

  /** 构建新的其他物体 */
  fun randomAccidentOther(
    parent: AccidentRegister,
    sn: Short = 0,
    name: String = random("name"),
    type: String = random("type")
  ): AccidentOther {
    return AccidentOther(
      parent = parent,
      sn = sn,
      name = name,
      type = type,
      belong = random("belong"),
      linkmanName = random("linkmanName"),
      linkmanPhone = random("linkmanPhone"),
      damageState = random("damageState"),
      damageMoney = BigDecimal("${randomInt(1, 100)}.00"),
      actualMoney = BigDecimal("${randomInt(1, 100)}.00"),
      followType = random("followType"),
      updatedTime = OffsetDateTime.now()
    )
  }
}