/**
 * 这个包只能用于单元测试，请不要在正式代码中引用。
 *
 * @author RJ
 */
package cn.gftaxi.traffic.accident.test

import cn.gftaxi.traffic.accident.bc.dto.CaseRelatedInfoDto
import cn.gftaxi.traffic.accident.common.*
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.dto.*
import cn.gftaxi.traffic.accident.po.*
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
    return AccidentCar().also {
      it.parent = parent
      it.sn = sn
      it.name = name
      it.type = type
      it.model = model
      it.towCount = randomInt(1, 10).toShort()
      it.repairType = randomString("repairType")
      it.guessTowMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.guessRepairMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.actualTowMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.actualRepairMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.damageState = randomString("damageState")
      it.followType = randomString("followType")
      it.updatedTime = OffsetDateTime.now()
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
    return AccidentPeople().also {
      it.parent = parent
      it.sn = sn
      it.name = name
      it.type = type
      it.sex = Sex.Male
      it.phone = phone
      it.transportType = randomString("transportType")
      it.duty = randomString("duty")
      it.damageState = randomString("damageState")
      it.guessTreatmentMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.guessCompensateMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.actualTreatmentMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.actualCompensateMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.followType = randomString("followType")
      it.updatedTime = OffsetDateTime.now()
    }
  }

  /** 构建新的其他物体 */
  fun randomAccidentOther(
    parent: AccidentCase,
    sn: Short = 0,
    name: String = randomString("name"),
    type: String = randomString("type")
  ): AccidentOther {
    return AccidentOther().also {
      it.parent = parent
      it.sn = sn
      it.name = name
      it.type = type
      it.belong = randomString("belong")
      it.linkmanName = randomString("linkmanName")
      it.linkmanPhone = randomString("linkmanPhone")
      it.damageState = randomString("damageState")
      it.guessMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.actualMoney = BigDecimal("${randomInt(1, 100)}.00")
      it.followType = randomString("followType")
      it.updatedTime = OffsetDateTime.now()
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
    registerStatus: AuditStatus? = null,
    overdueReport: Boolean? = null,
    reportStatus: AuditStatus? = null
  ): Pair<AccidentCase, AccidentSituation> {
    return Pair(
      AccidentCase().also {
        it.id = id
        it.code = code
        it.happenTime = happenTime
        it.carPlate = randomString("粤A.")
        it.driverName = randomString("driver")
        it.driverType = DriverType.Official
        it.location = randomString("location")
        it.motorcadeName = randomString("m")
        it.level = randomString("level")
        it.hitForm = randomString("hitForm")
        it.hitType = randomString("hitType")
        it.loadState = randomString("loadState")
      },
      AccidentSituation().also {
        it.id = id
        it.stage = stage

        // 报案信息
        it.overdueDraft = overdueDraft
        when (overdueDraft) {
          null -> it.draftTime = null
          true -> it.draftTime = happenTime.plusHours(12 + 1) // 超过 12 小时为逾期报案
          else -> it.draftTime = happenTime.plusHours(12 - 1)
        }
        it.draftStatus = draftStatus ?: when (stage) {
          CaseStage.ToSubmit -> DraftStatus.ToSubmit
          CaseStage.Drafting -> DraftStatus.Drafting
          else -> DraftStatus.Drafted
        }
        it.source = "BC"
        it.authorName = randomString("authorName")
        it.authorId = randomString("authorId")

        // 登记信息
        it.overdueRegister = overdueRegister
        when (overdueRegister) {
          null -> it.registerTime = null
          true -> it.registerTime = happenTime.plusHours(24 + 1) // 超过 24 小时为逾期登记
          else -> it.registerTime = happenTime.plusHours(24 - 1)
        }
        it.registerStatus = registerStatus ?: when (stage) {
          CaseStage.ToSubmit -> null
          CaseStage.Drafting -> null
          CaseStage.Registering -> AuditStatus.ToSubmit
          else -> AuditStatus.Approved
        }

        // 报告信息
        it.overdueReport = overdueReport
        when (overdueReport) {
          null -> it.reportTime = null
          true -> it.reportTime = happenTime.plusHours(48 + 1) // 超过 48 小时为逾期报告
          else -> it.reportTime = happenTime.plusHours(48 - 1)
        }
        it.reportStatus = reportStatus ?: when (stage) {
          CaseStage.ToSubmit -> null
          CaseStage.Drafting -> null
          CaseStage.Reporting -> AuditStatus.ToSubmit
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
    draftTime: OffsetDateTime = happenTime.plusHours(12 - 1),
    overdueDraft: Boolean? = isOverdue(happenTime, draftTime, 12 * 60 * 60),
    draftStatus: DraftStatus = DraftStatus.ToSubmit
  ): AccidentDraftDto4View {
    return AccidentDraftDto4View(
      id = id,
      code = code,
      happenTime = happenTime,

      draftTime = draftTime,
      overdueDraft = overdueDraft,
      draftStatus = draftStatus,

      motorcadeName = randomString("m"),
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
    draftTime: OffsetDateTime = happenTime.plusHours(12 - 1),
    overdueDraft: Boolean? = isOverdue(happenTime, draftTime, 12 * 60 * 60),
    draftStatus: DraftStatus = DraftStatus.ToSubmit
  ): AccidentDraftDto4Form {
    return AccidentDraftDto4Form().also {
      it.id = id
      it.code = code
      it.happenTime = happenTime

      it.draftTime = draftTime
      it.overdueDraft = overdueDraft
      it.draftStatus = draftStatus

      it.motorcadeName = randomString("m")
      it.carPlate = randomString("car")
      it.driverName = randomString("driver")
      it.driverType = DriverType.Official
      it.location = randomString("location")
      it.hitForm = randomString("hitForm")
      it.hitType = randomString("hitType")
      it.authorName = randomString("authorName")
    }
  }

  /** 构建新的登记视图的数据行 */
  fun randomAccidentRegisterDto4View(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    draftTime: OffsetDateTime = happenTime.plusHours(12 - 1),
    overdueDraft: Boolean = isOverdue(happenTime, draftTime, 12 * 60 * 60),
    registerTime: OffsetDateTime = happenTime.plusHours(24 - 1),
    overdueRegister: Boolean? = isOverdue(happenTime, registerTime, 24 * 60 * 60),
    registerStatus: AuditStatus = AuditStatus.ToSubmit
  ): AccidentRegisterDto4View {
    return AccidentRegisterDto4View(
      id = id,
      code = code,
      happenTime = happenTime,

      draftTime = draftTime,
      overdueDraft = overdueDraft,

      registerTime = registerTime,
      overdueRegister = overdueRegister,
      registerStatus = registerStatus,

      motorcadeName = randomString("m"),
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
    draftTime: OffsetDateTime = happenTime.plusHours(12 - 1),
    overdueDraft: Boolean = isOverdue(happenTime, draftTime, 12 * 60 * 60),
    draftStatus: DraftStatus = DraftStatus.Drafted,
    registerTime: OffsetDateTime = happenTime.plusHours(24 - 1),
    overdueRegister: Boolean? = isOverdue(happenTime, registerTime, 24 * 60 * 60),
    registerStatus: AuditStatus = AuditStatus.ToSubmit
  ): AccidentRegisterDto4Form {
    return AccidentRegisterDto4Form().also {
      it.id = id
      it.code = code
      it.happenTime = happenTime

      it.draftTime = draftTime
      it.overdueDraft = overdueDraft
      it.draftStatus = draftStatus

      it.registerTime = registerTime
      it.overdueRegister = overdueRegister
      it.registerStatus = registerStatus

      it.motorcadeName = randomString("m")
      it.carPlate = randomString("car")
      it.driverName = randomString("driver")
      it.driverType = DriverType.Official
      it.location = randomString("location")
      it.hitForm = randomString("hitForm")
      it.hitType = randomString("hitType")
      it.authorName = randomString("authorName")
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

  /** 构建新的报告视图的数据行 */
  fun randomAccidentReportDto4View(
    id: Int? = nextId("Case"),
    happenTime: OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusDays(10),
    code: String = nextCaseCode(happenTime),
    draftTime: OffsetDateTime = happenTime.plusHours(12 - 1),
    overdueDraft: Boolean = isOverdue(happenTime, draftTime, 12 * 60 * 60),
    registerTime: OffsetDateTime = happenTime.plusHours(24 - 1),
    overdueRegister: Boolean = isOverdue(happenTime, registerTime, 24 * 60 * 60),
    reportTime: OffsetDateTime? = happenTime.plusHours(48 - 1),
    overdueReport: Boolean? = isOverdue(happenTime, registerTime, 48 * 60 * 60),
    reportStatus: AuditStatus = AuditStatus.ToSubmit
  ): AccidentReportDto4View {
    return AccidentReportDto4View(
      id = id,
      code = code,
      happenTime = happenTime,

      draftTime = draftTime,
      overdueDraft = overdueDraft,

      overdueRegister = overdueRegister,
      registerTime = registerTime,

      reportTime = reportTime,
      overdueReport = overdueReport,
      reportStatus = reportStatus,

      motorcadeName = randomString("m"),
      carPlate = randomString("car"),
      carModel = randomString("carModel"),
      driverName = randomString("driver"),
      driverType = DriverType.Official,

      location = randomString("location"),
      hitForm = randomString("hitForm"),
      level = randomString("level"),
      duty = randomString("duty"),
      appointDriverReturnTime = happenTime.plusHours(48 - 1)
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