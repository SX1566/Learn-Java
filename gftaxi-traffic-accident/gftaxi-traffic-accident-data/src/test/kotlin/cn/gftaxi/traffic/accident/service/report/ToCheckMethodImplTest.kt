package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_REPORT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_NOT_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.Utils.calculateOverdueDayAndHour
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.service.AccidentReportServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomAuthenticatedUser
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.eq
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.operation.OperationType.Confirmation
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Target
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

/**
 * Test [AccidentReportServiceImpl.toCheck].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentReportServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class ToCheckMethodImplTest @Autowired constructor(
  @Value("\${app.report-overdue-hours:48}") private val reportOverdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentReportService: AccidentReportService,
  private val operationService: OperationService
) {
  private val reportOverdueSeconds = reportOverdueHours * 60 * 60
  @Test
  fun `Success by allow status`() {
    successByAllowStatus(AuditStatus.ToSubmit)
    successByAllowStatus(AuditStatus.Rejected)
  }

  private fun successByAllowStatus(status: AuditStatus) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)
    Mockito.reset(operationService)

    // mock
    val id = 1
    val pair = randomCase(reportStatus = status)
    val updateData = when (status) {
    // 首次提交
      AuditStatus.ToSubmit -> mapOf(
        "stage" to CaseStage.Reporting,
        "reportStatus" to AuditStatus.ToCheck
      )
    // 非首次提交
      else -> mapOf("reportStatus" to AuditStatus.ToCheck)
    }
    val user = randomAuthenticatedUser()
    val operation = Operation(
      time = OffsetDateTime.now(),
      type = Confirmation.name,
      target = Target(id = id.toString(), type = ACCIDENT_REPORT_TARGET_TYPE),
      operator = user.toOperator(),
      cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
      title = operationTitles[Confirmation.name + ACCIDENT_REPORT_TARGET_TYPE]!!
    )
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getCaseSituation(id)).thenReturn(pair.toMono())
    `when`(accidentDao.update(
      id = eq(id),
      data = argThat {
        filter { updateData.containsKey(it.key) } == updateData
          && if (status == AuditStatus.ToSubmit) {
          !(this["reportTime"] as OffsetDateTime).isBefore(operation.time)
            && this["overdueReport"] == isOverdue(pair.first.happenTime!!,
            this["reportTime"] as OffsetDateTime, reportOverdueSeconds)
        } else true
      },
      targetType = eq(ACCIDENT_REPORT_TARGET_TYPE),
      generateLog = eq(false)
    )).thenReturn(Mono.empty())
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(user)))
    `when`(operationService.create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && title == operation.title
        && result == if (status == AuditStatus.ToSubmit) {
        calculateOverdueDayAndHour(pair.first.happenTime!!, time, reportOverdueSeconds)
          .let {
            if (it == "") CONFIRMATION_NOT_OVERDUE_RESUTLT
            else CONFIRMATION_OVERDUE_RESUTLT + it
          }
      } else null
    })).thenReturn(Mono.empty())

    // invoke
    val actual = accidentReportService.toCheck(id)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_SUBMIT)
    verify(accidentDao).getCaseSituation(id)
    verify(accidentDao).update(
      id = eq(id),
      data = argThat {
        filter { updateData.containsKey(it.key) } == updateData
          && if (status == AuditStatus.ToSubmit) {
          !(this["reportTime"] as OffsetDateTime).isBefore(operation.time)
            && this["overdueReport"] == isOverdue(pair.first.happenTime!!,
            this["reportTime"] as OffsetDateTime, reportOverdueSeconds)
        } else true
      },
      targetType = eq(ACCIDENT_REPORT_TARGET_TYPE),
      generateLog = eq(false)
    )
    verify(securityService).getAuthenticatedUser()
    verify(operationService).create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && title == operation.title
        && result == if (status == AuditStatus.ToSubmit) {
        calculateOverdueDayAndHour(pair.first.happenTime!!, time, reportOverdueSeconds)
          .let {
            if (it == "") CONFIRMATION_NOT_OVERDUE_RESUTLT
            else CONFIRMATION_OVERDUE_RESUTLT + it
          }
      } else null
    })
  }

  @Test
  fun `Failed by illegal status`() {
    AuditStatus.values()
      .filterNot { it == AuditStatus.ToSubmit || it == AuditStatus.Rejected }
      .forEach { failedByIllegalStatus(it) }
  }

  private fun failedByIllegalStatus(status: AuditStatus) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)

    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getCaseSituation(id)).thenReturn(randomCase(reportStatus = status).toMono())

    // invoke
    val actual = accidentReportService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_SUBMIT)
    verify(accidentDao).getCaseSituation(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getCaseSituation(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentReportService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_SUBMIT)
    verify(accidentDao).getCaseSituation(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_SUBMIT)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentReportService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_SUBMIT)
  }
}