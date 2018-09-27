/**
 * 这个包只能用于单元测试，请不要在正式代码中引用。
 *
 * @author RJ
 */
package cn.gftaxi.traffic.accident.test

import cn.gftaxi.traffic.accident.bc.dto.CaseRelatedInfoDto
import cn.gftaxi.traffic.accident.common.*
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.*
import org.slf4j.LoggerFactory
import tech.simter.operation.po.Attachment
import tech.simter.reactive.context.SystemContext
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * 单元测试辅助工具类。
 *
 * @author RJ
 */
object TestUtils {
  private val logger = LoggerFactory.getLogger(TestUtils::class.java)

  private var ymdMap = hashMapOf<String, Int>()
  /** 获取下一个事故编号 */
  fun nextCaseCode(happenTime: OffsetDateTime): String {
    val ymd = happenTime.format(FORMAT_TO_YYYYMMDD)
    if (!ymdMap.containsKey(ymd)) ymdMap[ymd] = 1
    else ymdMap[ymd] = ymdMap[ymd]!! + 1
    return "${ymd}_${ymdMap[ymd]}"
  }

  private var idMap = hashMapOf<String, Int>()
  /** 获取指定类型的下一个自动 ID 值 */
  fun nextId(type: String = "T"): Int {
    if (!idMap.containsKey(type)) idMap[type] = 1
    else idMap[type] = idMap[type]!! + 1
    return idMap[type]!!
  }

  /** 在指定区间随机生成一个数字 */
  fun randomInt(start: Int = 0, end: Int = 100) = Random().nextInt(end + 1 - start) + start

  private var strMap = hashMapOf("粤A." to 100000)
  /** 生成带固定前缀的唯一字符串 */
  fun randomString(prefix: String = "Str"): String {
    if (!strMap.containsKey(prefix)) strMap[prefix] = 1
    else strMap[prefix] = strMap[prefix]!! + 1
    return "$prefix${strMap[prefix]}"
  }

  /** 构建新的认证用户信息 */
  fun randomAuthenticatedUser(
    id: Int = randomInt(),
    account: String = randomString(),
    name: String = randomString()
  ): SystemContext.User {
    return SystemContext.User(id = id, account = account, name = name)
  }

  /** 构建新的附件 */
  fun randomAttachment(): Attachment {
    return Attachment(
      id = randomString(),
      name = randomString(),
      ext = randomString(),
      size = randomInt()
    )
  }

  /** 构建新的当事车辆 */
  fun randomAccidentCar(
    parent: AccidentCase,
    sn: Short = 0,
    name: String = randomString("name"),
    type: String = randomString("type"),
    model: String = randomString("model")
  ): AccidentCar {
    return AccidentCar().apply {
      this.parent = parent
      this.sn = sn
      this.name = name
      this.type = type
      this.model = model
      this.towCount = randomInt(1, 10).toShort()
      this.repairType = randomString("repairType")
      this.guessTowMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.guessRepairMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.actualTowMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.actualRepairMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.damageState = randomString("damageState")
      this.followType = randomString("followType")
      this.updatedTime = OffsetDateTime.now()
    }
  }

  /** 构建新的当事人 */
  fun randomAccidentPeople(
    parent: AccidentCase,
    sn: Short = 0,
    name: String = randomString("name"),
    type: String = randomString("type"),
    phone: String = randomString("phone")
  ): AccidentPeople {
    return AccidentPeople().apply {
      this.parent = parent
      this.sn = sn
      this.name = name
      this.type = type
      this.sex = Sex.Male
      this.phone = phone
      this.transportType = randomString("transportType")
      this.duty = randomString("duty")
      this.damageState = randomString("damageState")
      this.guessTreatmentMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.guessCompensateMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.actualTreatmentMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.actualCompensateMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.followType = randomString("followType")
      this.updatedTime = OffsetDateTime.now()
    }
  }

  /** 构建新的其他物体 */
  fun randomAccidentOther(
    parent: AccidentCase,
    sn: Short = 0,
    name: String = randomString("name"),
    type: String = randomString("type")
  ): AccidentOther {
    return AccidentOther().apply {
      this.parent = parent
      this.sn = sn
      this.name = name
      this.type = type
      this.belong = randomString("belong")
      this.linkmanName = randomString("linkmanName")
      this.linkmanPhone = randomString("linkmanPhone")
      this.damageState = randomString("damageState")
      this.guessMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.actualMoney = BigDecimal("${randomInt(1, 100)}.00")
      this.followType = randomString("followType")
      this.updatedTime = OffsetDateTime.now()
    }
  }

  /** 构建新的案件 */
  fun randomCase(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    stage: CaseStage = CaseStage.ToSubmit,
    overdueDraft: Boolean? = null,
    draftStatus: DraftStatus? = null,
    overdueRegister: Boolean? = null,
    registerStatus: AuditStatus? = null
  ): Pair<AccidentCase, AccidentSituation> {
    return Pair(
      AccidentCase().apply {
        this.id = id
        this.code = code
        this.happenTime = happenTime
        this.carPlate = randomString("粤A.")
        this.driverName = randomString("driver")
        this.driverType = DriverType.Official
        this.location = randomString("location")
        this.motorcadeName = randomString("m")
        this.level = randomString("level")
        this.hitForm = randomString("hitForm")
        this.hitType = randomString("hitType")
        this.loadState = randomString("loadState")
      },
      AccidentSituation().apply {
        this.id = id
        this.stage = stage

        // 报案信息
        this.overdueDraft = overdueDraft
        when (overdueDraft) {
          null -> this.draftTime = null
          true -> this.draftTime = happenTime.plusHours(12 + 1) // 超过 12 小时为逾期报案
          else -> this.draftTime = happenTime.plusHours(12 - 1)
        }
        this.draftStatus = draftStatus ?: when (stage) {
          CaseStage.ToSubmit -> DraftStatus.ToSubmit
          CaseStage.Drafting -> DraftStatus.Drafting
          else -> DraftStatus.Drafted
        }
        this.source = "BC"
        this.authorName = randomString("authorName")
        this.authorId = randomString("authorId")

        // 登记信息
        this.overdueRegister = overdueRegister
        when (overdueRegister) {
          null -> this.registerTime = null
          true -> this.registerTime = happenTime.plusHours(24 + 1) // 超过 24 小时为逾期登记
          else -> this.registerTime = happenTime.plusHours(24 - 1)
        }
        this.registerStatus = registerStatus ?: when (stage) {
          CaseStage.ToSubmit -> null
          CaseStage.Drafting -> null
          CaseStage.Registering -> AuditStatus.ToSubmit
          else -> AuditStatus.Approved
        }
      }
    )
  }

  /** 构建新的报案视图的数据行 */
  fun randomAccidentDraftDto4View(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    draftTime: OffsetDateTime = happenTime.plusHours(1),
    overdueDraft: Boolean = Utils.isOverdue(happenTime, draftTime, 12 * 60 * 60),
    draftStatus: DraftStatus = DraftStatus.ToSubmit
  ): AccidentDraftDto4View {
    return AccidentDraftDto4View(
      id = id,
      code = code,
      happenTime = happenTime,
      draftStatus = draftStatus,
      overdueDraft = overdueDraft,
      draftTime = draftTime,
      carPlate = randomString("car"),
      driverName = randomString("driver"),
      location = randomString("location"),
      hitForm = randomString("hitForm"),
      hitType = randomString("hitType"),
      authorName = randomString("authorName")
    )
  }

  /** 构建新的报案表单数据 */
  fun randomAccidentDraftDto4Form(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    draftTime: OffsetDateTime = happenTime.plusHours(1),
    overdueDraft: Boolean = Utils.isOverdue(happenTime, draftTime, 12 * 60 * 60),
    draftStatus: DraftStatus = DraftStatus.ToSubmit
  ): AccidentDraftDto4Form {
    return AccidentDraftDto4Form().apply {
      this.id = id
      this.code = code
      this.happenTime = happenTime
      this.draftStatus = draftStatus
      this.overdueDraft = overdueDraft
      this.draftTime = draftTime
      this.carPlate = randomString("car")
      this.driverName = randomString("driver")
      this.driverType = DriverType.Official
      this.location = randomString("location")
      this.hitForm = randomString("hitForm")
      this.hitType = randomString("hitType")
      this.authorName = randomString("authorName")
    }
  }

  /** 构建新的登记视图的数据行 */
  fun randomAccidentRegisterDto4View(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    registerTime: OffsetDateTime = happenTime.plusHours(13),
    overdueRegister: Boolean = Utils.isOverdue(happenTime, registerTime, 12 * 60 * 60),
    registerStatus: AuditStatus = AuditStatus.ToSubmit
  ): AccidentRegisterDto4View {
    return AccidentRegisterDto4View(
      id = id,
      code = code,
      happenTime = happenTime,
      registerStatus = registerStatus,
      overdueRegister = overdueRegister,
      registerTime = registerTime,
      draftTime = happenTime.plusHours(1),
      overdueDraft = false,
      carPlate = randomString("car"),
      driverName = randomString("driver"),
      location = randomString("location"),
      hitForm = randomString("hitForm"),
      hitType = randomString("hitType"),
      authorName = randomString("authorName")
    )
  }

  /** 构建新的登记表单数据 */
  fun randomAccidentRegisterDto4Form(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    registerTime: OffsetDateTime = happenTime.plusHours(13),
    overdueRegister: Boolean = Utils.isOverdue(happenTime, registerTime, 12 * 60 * 60),
    registerStatus: AuditStatus = AuditStatus.ToSubmit
  ): AccidentRegisterDto4Form {
    return AccidentRegisterDto4Form().apply {
      this.id = id
      this.code = code
      this.happenTime = happenTime
      this.registerStatus = registerStatus
      this.registerTime = registerTime
      this.overdueRegister = overdueRegister
      this.draftStatus = draftStatus
      this.draftStatus = DraftStatus.Drafted
      this.overdueDraft = overdueDraft
      this.draftTime = happenTime.plusHours(1)
      this.overdueDraft = false
      this.carPlate = randomString("car")
      this.driverName = randomString("driver")
      this.driverType = DriverType.Official
      this.location = randomString("location")
      this.hitForm = randomString("hitForm")
      this.hitType = randomString("hitType")
      this.authorName = randomString("authorName")
    }
  }

  /** 构建新的登记汇总统计数据 */
  fun randomAccidentRegisterDto4StatSummary(
    scope: String = randomString("RM")
  ): AccidentRegisterDto4StatSummary {
    val checked = randomInt(0, 100)
    val checking = randomInt(0, 100)
    val drafting = randomInt(0, 100)
    val total = drafting + checking + checked
    return AccidentRegisterDto4StatSummary(
      scope = scope,
      checked = checked,
      checking = checking,
      drafting = drafting,
      total = total,
      overdueDraft = randomInt(0, total),
      overdueRegister = randomInt(0, total)
    )
  }

  fun randomCaseRelatedInfoDto(): CaseRelatedInfoDto {
    val now = OffsetDateTime.now()
    return CaseRelatedInfoDto(
      // 车队
      motorcadeName = randomString("mc"),

      // 合同信息
      contractType = randomString("contractType"),
      contractDrivers = randomString("contractDrivers"),

      // 车辆信息
      carId = nextId("car"),
      carModel = randomString("carModel"),
      carOperateDate = now.minusYears(3).toLocalDate(),

      // 司机信息
      driverId = nextId("driver"),
      driverUid = randomString("driverUid"),
      driverType = DriverType.Official,
      driverPhone = randomString("driverPhone"),
      driverHiredDate = now.minusYears(2).toLocalDate(),
      driverBirthDate = now.minusYears(30).toLocalDate(),
      driverIdentityCode = randomString("driverIdentityCode"),
      driverServiceCode = randomString("driverServiceCode"),
      driverOrigin = randomString("driverOrigin"),
      driverLicenseDate = now.minusYears(10).toLocalDate(),
      relatedDriverName = randomString("relatedDriverName"),
      relatedDriverPhone = randomString("relatedDriverPhone"),

      // 历史统计
      historyAccidentCount = randomInt(0, 10).toShort(),
      historyTrafficOffenceCount = randomInt(0, 10).toShort(),
      historyServiceOffenceCount = randomInt(0, 10).toShort(),
      historyComplainCount = randomInt(0, 10).toShort()
    )
  }
}