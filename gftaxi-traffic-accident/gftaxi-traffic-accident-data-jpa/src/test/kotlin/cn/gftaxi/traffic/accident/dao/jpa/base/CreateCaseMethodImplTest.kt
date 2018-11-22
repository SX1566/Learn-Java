package cn.gftaxi.traffic.accident.dao.jpa.base

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Sex
import cn.gftaxi.traffic.accident.common.Utils.FORMAT_TO_YYYYMMDD
import cn.gftaxi.traffic.accident.common.Utils.calculateYears
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.jpa.AccidentDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.po.AccidentCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomAccidentDraftDto4FormSubmit
import cn.gftaxi.traffic.accident.test.TestUtils.randomCaseRelatedInfoDto
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.reflect.full.memberProperties

/**
 * Test [AccidentDaoImpl.createCase].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(ModuleConfiguration::class, tech.simter.operation.dao.jpa.ModuleConfiguration::class)
@DataJpaTest
@MockBean(BcDao::class, ReactiveSecurityService::class)
class CreateCaseMethodImplTest @Autowired constructor(
  private val accidentDao: AccidentDao,
  private val bcDao: BcDao
) {
  private val overdueSeconds = 12L * 60 * 60 // 12h
  @Test
  fun success() {
    // init data
    val now = OffsetDateTime.now()
    val happenTime: OffsetDateTime = now.truncatedTo(ChronoUnit.MINUTES).minusDays(10)
    val code = happenTime.format(FORMAT_TO_YYYYMMDD) + "_01"
    val toSubmitDto = randomAccidentDraftDto4FormSubmit(happenTime = happenTime)
    val bcInfoDto = randomCaseRelatedInfoDto()
    `when`(bcDao.getCaseRelatedInfo(
      carPlate = toSubmitDto.carPlate!!,
      driverName = toSubmitDto.driverName!!,
      date = happenTime.toLocalDate()
    )).thenReturn(bcInfoDto.toMono())

    // invoke and verify
    StepVerifier.create(accidentDao.createCase(toSubmitDto))
      .consumeNextWith { (case, situation) ->
        val caseProperties = AccidentCase::class.memberProperties.associate { it.name to it }
        val toSubmitDtoProperties = toSubmitDto.javaClass.kotlin.memberProperties
        val bcInfoDtoProperties = bcInfoDto.javaClass.kotlin.memberProperties

        // 验证 situation 的属性值
        assertEquals(CaseStage.Drafting, situation.stage)
        assertEquals(DraftStatus.Drafting, situation.draftStatus)
        assertFalse(situation.draftTime!!.isBefore(now))
        assertEquals(isOverdue(happenTime, situation.draftTime!!, overdueSeconds), situation.overdueDraft!!)
        assertEquals(AuditStatus.ToSubmit, situation.registerStatus)
        assertNull(situation.registerTime)
        assertNull(situation.overdueRegister)
        assertNull(situation.reportStatus)
        assertNull(situation.overdueReport)

        // 验证 case 的属性值
        assertNotNull(case.id)
        assertEquals(case.id, situation.id)
        assertEquals(code, case.code)

        // case 中可以通过反射验证的属性
        val excludePropertiesNames = listOf(
          "holder",
          "data",
          "source",
          "authorId",
          "authorName",
          "contractType",
          "contractDrivers",
          "driverBirthDate",
          "driverLicenseDate",
          "relatedDriverName",
          "relatedDriverPhone",
          "driverUid"
        )
        toSubmitDtoProperties.filterNot { excludePropertiesNames.contains(it.name) }.forEach {
          assertEquals(it.get(toSubmitDto), caseProperties[it.name]?.get(case))
        }
        bcInfoDtoProperties.filterNot { excludePropertiesNames.contains(it.name) }.forEach {
          assertEquals(it.get(bcInfoDto), caseProperties[it.name]?.get(case))
        }

        // case 中的特殊属性
        assertEquals(bcInfoDto.contractType, case.carContractType)
        assertEquals(bcInfoDto.contractDrivers, case.carContractDrivers)
        assertEquals(bcInfoDto.driverUid!!.let { "S:$it" }, case.driverPicId)
        assertEquals(calculateYears(bcInfoDto.driverBirthDate!!, LocalDate.now()).toBigDecimal(), case.driverAge)
        assertEquals(calculateYears(bcInfoDto.driverLicenseDate!!, LocalDate.now()).toBigDecimal(), case.driverDriveYears)
        assertEquals("[对班] ${bcInfoDto.relatedDriverName}", case.driverLinkmanName)
        assertEquals("[对班] ${bcInfoDto.relatedDriverPhone}", case.driverLinkmanPhone)

        // 验证生成了一条自车类型的当事车辆信息
        assertEquals(1, case.cars!!.size)
        val car = case.cars!![0]
        assertNotNull(car.id)
        assertEquals(0.toShort(), car.sn)
        assertEquals("自车", car.type)
        assertEquals("出租车", car.model)
        assertEquals(toSubmitDto.carPlate, car.name)
        assertFalse(car.updatedTime!!.isBefore(now))

        // 验证生成了一条自车类型的当事人信息
        assertEquals(1, case.peoples!!.size)
        val people = case.peoples!![0]
        assertNotNull(people.id)
        assertEquals(0.toShort(), people.sn)
        assertEquals("自车", people.type)
        assertEquals(Sex.NotSet, people.sex)
        assertEquals(toSubmitDto.driverName, people.name)
        assertFalse(people.updatedTime!!.isBefore(now))
      }
      .verifyComplete()
    verify(bcDao).getCaseRelatedInfo(
      carPlate = toSubmitDto.carPlate!!,
      driverName = toSubmitDto.driverName!!,
      date = happenTime.toLocalDate()
    )
  }
}