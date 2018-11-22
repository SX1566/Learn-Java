package cn.gftaxi.traffic.accident.service.report

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_REPORT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.APPROVAL_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.REJECTION_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REPORT_CHECK
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.CheckedInfoDto
import cn.gftaxi.traffic.accident.service.AccidentReportService
import cn.gftaxi.traffic.accident.service.AccidentReportServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomAttachment
import cn.gftaxi.traffic.accident.test.TestUtils.randomAuthenticatedUser
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import com.nhaarman.mockito_kotlin.argThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.exception.ForbiddenException
import tech.simter.exception.NotFoundException
import tech.simter.exception.PermissionDeniedException
import tech.simter.operation.OperationType.Approval
import tech.simter.operation.OperationType.Rejection
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Target
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*

/**
 * Test [AccidentReportServiceImpl.checked].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentReportServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class CheckedMethodImplTest @Autowired constructor(
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentReportService: AccidentReportService,
  private val operationService: OperationService
) {
  @Test
  fun `Success checked`() {
    success(true)  // 审核通过
    success(false) // 审核不通过
  }

  private fun success(passed: Boolean) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)
    Mockito.reset(operationService)

    // mock
    val id = 1
    val situation = randomCase(stage = CaseStage.Reporting, reportStatus = AuditStatus.ToCheck).second
    val checkedInfo = CheckedInfoDto(
      passed = passed,
      comment = "test",
      attachment = if (passed) null else randomAttachment()
    )
    val updateData = when (passed) {
    // 审核通过
      true -> mapOf(
        "reportStatus" to AuditStatus.Approved,
        "stage" to CaseStage.Following,
        "reportCheckedCount" to (situation.reportCheckedCount ?: 0) + 1,
        "reportCheckedComment" to null,
        "reportCheckedAttachments" to null
      )
    // 审核不通过
      else -> mapOf(
        "reportStatus" to AuditStatus.Rejected,
        "reportCheckedCount" to (situation.reportCheckedCount ?: 0) + 1,
        "reportCheckedComment" to checkedInfo.comment,
        "reportCheckedAttachments" to checkedInfo.attachment?.run { listOf(checkedInfo.attachment) }
      )
    }
    val user = randomAuthenticatedUser()
    val type = if (checkedInfo.passed) Approval.name else Rejection.name
    val operation = Operation(
      time = OffsetDateTime.now(),
      type = type,
      target = Target(id = id.toString(), type = ACCIDENT_REPORT_TARGET_TYPE),
      operator = user.toOperator(),
      cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
      comment = checkedInfo.comment,
      attachments = checkedInfo.attachment?.let { listOf(it) },
      result = if (checkedInfo.passed) APPROVAL_RESUTLT else REJECTION_RESUTLT,
      title = operationTitles[type + ACCIDENT_REPORT_TARGET_TYPE]!!
    )
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_CHECK)).thenReturn(Mono.empty())
    `when`(accidentDao.getSituation(id)).thenReturn(situation.toMono())
    `when`(accidentDao.update(
      id = id,
      data = updateData,
      targetType = ACCIDENT_REPORT_TARGET_TYPE,
      generateLog = false
    )).thenReturn(Mono.empty())
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(user)))
    `when`(operationService.create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && comment == operation.comment && attachments == operation.attachments
        && result == operation.result && title == operation.title
    })).thenReturn(Mono.empty())

    // invoke
    val actual = accidentReportService.checked(id, checkedInfo)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_CHECK)
    verify(accidentDao).getSituation(id)
    verify(accidentDao).update(
      id = id,
      data = updateData,
      targetType = ACCIDENT_REPORT_TARGET_TYPE,
      generateLog = false
    )
    verify(securityService).getAuthenticatedUser()
    verify(operationService).create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && comment == operation.comment && attachments == operation.attachments
        && result == operation.result && title == operation.title
    })
  }

  @Test
  fun `Failed by illegal status`() {
    AuditStatus.values()
      .filterNot { it == AuditStatus.ToCheck }
      .forEach { failedByIllegalStatus(it) }
  }

  private fun failedByIllegalStatus(status: AuditStatus) {
    // reset
    Mockito.reset(securityService)
    Mockito.reset(accidentDao)

    // mock
    val id = 1
    val situation = randomCase(registerStatus = status).second
    val checkedInfo = CheckedInfoDto(passed = true)
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_CHECK)).thenReturn(Mono.empty())
    `when`(accidentDao.getSituation(id)).thenReturn(situation.toMono())

    // invoke
    val actual = accidentReportService.checked(id, checkedInfo)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_CHECK)
    verify(accidentDao).getSituation(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    val checkedInfo = CheckedInfoDto(passed = true)
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_CHECK)).thenReturn(Mono.empty())
    `when`(accidentDao.getSituation(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentReportService.checked(id, checkedInfo)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_CHECK)
    verify(accidentDao).getSituation(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    val dto = CheckedInfoDto(passed = true)
    `when`(securityService.verifyHasAnyRole(ROLE_REPORT_CHECK)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentReportService.checked(id, dto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REPORT_CHECK)
  }
}