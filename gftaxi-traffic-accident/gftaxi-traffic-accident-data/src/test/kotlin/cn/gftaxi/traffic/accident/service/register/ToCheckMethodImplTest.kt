package cn.gftaxi.traffic.accident.service.register

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_REGISTER_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_NOT_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CONFIRMATION_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_REGISTER_SUBMIT
import cn.gftaxi.traffic.accident.common.AuditStatus
import cn.gftaxi.traffic.accident.common.CaseStage
import cn.gftaxi.traffic.accident.common.DraftStatus
import cn.gftaxi.traffic.accident.common.Utils.calculateOverdueDayAndHour
import cn.gftaxi.traffic.accident.common.Utils.isOverdue
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.service.AccidentRegisterService
import cn.gftaxi.traffic.accident.service.AccidentRegisterServiceImpl
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
 * Test [AccidentRegisterServiceImpl.toCheck].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentRegisterServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class ToCheckMethodImplTest @Autowired constructor(
  @Value("\${app.register-overdue-hours:24}") private val registerOverdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentRegisterService: AccidentRegisterService,
  private val operationService: OperationService
) {
  private val registerOverdueSeconds = registerOverdueHours * 60 * 60
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
    val pair = randomCase(registerStatus = status)
    val updateData = when (status) {
    // 首次提交
      AuditStatus.ToSubmit -> mapOf(
        "stage" to CaseStage.Registering,
        "draftStatus" to DraftStatus.Drafted,
        "registerStatus" to AuditStatus.ToCheck
      )
    // 非首次提交
      else -> mapOf("registerStatus" to AuditStatus.ToCheck)
    }
    val user = randomAuthenticatedUser()
    val operation = Operation(
      time = OffsetDateTime.now(),
      type = Confirmation.name,
      target = Target(id = id.toString(), type = ACCIDENT_REGISTER_TARGET_TYPE),
      operator = user.toOperator(),
      cluster = "$ACCIDENT_OPERATION_CLUSTER-$id",
      title = operationTitles[Confirmation.name + ACCIDENT_REGISTER_TARGET_TYPE]!!
    )
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getCaseSituation(id)).thenReturn(pair.toMono())
    `when`(accidentDao.update(
      id = eq(id),
      data = argThat {
        filter { updateData.containsKey(it.key) } == updateData
          && if (status == AuditStatus.ToSubmit) {
          !(this["registerTime"] as OffsetDateTime).isBefore(operation.time)
            && this["overdueRegister"] == isOverdue(pair.first.happenTime!!,
            this["registerTime"] as OffsetDateTime, registerOverdueSeconds)
        } else true
      },
      targetType = eq(ACCIDENT_REGISTER_TARGET_TYPE),
      generateLog = eq(false)
    )).thenReturn(Mono.empty())
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(user)))
    `when`(operationService.create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && title == operation.title
        && result == if (status == AuditStatus.ToSubmit) {
        calculateOverdueDayAndHour(pair.first.happenTime!!, time, registerOverdueSeconds)
          .let {
            if (it == "") CONFIRMATION_NOT_OVERDUE_RESUTLT
            else CONFIRMATION_OVERDUE_RESUTLT + it
          }
      } else null
    })).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
    verify(accidentDao).getCaseSituation(id)
    verify(accidentDao).update(
      id = eq(id),
      data = argThat {
        filter { updateData.containsKey(it.key) } == updateData
          && if (status == AuditStatus.ToSubmit) {
          !(this["registerTime"] as OffsetDateTime).isBefore(operation.time)
            && this["overdueRegister"] == isOverdue(pair.first.happenTime!!,
            this["registerTime"] as OffsetDateTime, registerOverdueSeconds)
        } else true
      },
      targetType = eq(ACCIDENT_REGISTER_TARGET_TYPE),
      generateLog = eq(false)
    )
    verify(securityService).getAuthenticatedUser()
    verify(operationService).create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && title == operation.title
        && result == if (status == AuditStatus.ToSubmit) {
        calculateOverdueDayAndHour(pair.first.happenTime!!, time, registerOverdueSeconds)
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
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getCaseSituation(id)).thenReturn(randomCase(registerStatus = status).toMono())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(ForbiddenException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
    verify(accidentDao).getCaseSituation(id)
  }

  @Test
  fun `Failed by NotFound`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.empty())
    `when`(accidentDao.getCaseSituation(id)).thenReturn(Mono.empty())

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(NotFoundException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
    verify(accidentDao).getCaseSituation(id)
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    val id = 1
    `when`(securityService.verifyHasAnyRole(ROLE_REGISTER_SUBMIT)).thenReturn(Mono.error(PermissionDeniedException()))

    // invoke
    val actual = accidentRegisterService.toCheck(id)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_REGISTER_SUBMIT)
  }
}