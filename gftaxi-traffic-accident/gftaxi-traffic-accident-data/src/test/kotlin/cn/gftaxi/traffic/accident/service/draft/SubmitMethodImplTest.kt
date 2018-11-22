package cn.gftaxi.traffic.accident.service.draft

import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_DRAFT_TARGET_TYPE
import cn.gftaxi.traffic.accident.common.AccidentOperation.ACCIDENT_OPERATION_CLUSTER
import cn.gftaxi.traffic.accident.common.AccidentOperation.CREATION_NOT_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.CREATION_OVERDUE_RESUTLT
import cn.gftaxi.traffic.accident.common.AccidentOperation.operationTitles
import cn.gftaxi.traffic.accident.common.AccidentRole.ROLE_DRAFT_SUBMIT
import cn.gftaxi.traffic.accident.common.Utils.calculateOverdueDayAndHour
import cn.gftaxi.traffic.accident.common.toOperator
import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4FormSubmit
import cn.gftaxi.traffic.accident.service.AccidentDraftService
import cn.gftaxi.traffic.accident.service.AccidentDraftServiceImpl
import cn.gftaxi.traffic.accident.test.TestUtils.randomAuthenticatedUser
import cn.gftaxi.traffic.accident.test.TestUtils.randomBoolean
import cn.gftaxi.traffic.accident.test.TestUtils.randomCase
import cn.gftaxi.traffic.accident.test.TestUtils.randomString
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.test.StepVerifier
import tech.simter.exception.NonUniqueException
import tech.simter.exception.PermissionDeniedException
import tech.simter.operation.OperationType
import tech.simter.operation.OperationType.Creation
import tech.simter.operation.po.Operation
import tech.simter.operation.po.Target
import tech.simter.operation.service.OperationService
import tech.simter.reactive.security.ReactiveSecurityService
import java.time.OffsetDateTime
import java.util.*


/**
 * Test [AccidentDraftServiceImpl.submit].
 *
 * @author RJ
 * @author zh
 */
@SpringJUnitConfig(AccidentDraftServiceImpl::class)
@MockBean(AccidentDao::class, ReactiveSecurityService::class, OperationService::class)
class SubmitMethodImplTest @Autowired constructor(
  @Value("\${app.draft-overdue-hours:12}") private val draftOverdueHours: Long,
  private val securityService: ReactiveSecurityService,
  private val accidentDao: AccidentDao,
  private val accidentDraftService: AccidentDraftService,
  private val operationService: OperationService
) {
  private val draftOverdueSeconds = draftOverdueHours * 60 * 60
  @Test
  fun `Success submit`() {
    // mock
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT)).thenReturn(Mono.empty())
    val pair = randomCase(overdueDraft = randomBoolean())
    val dto = AccidentDraftDto4FormSubmit().apply {
      this.happenTime = pair.first.happenTime
      this.carPlate = pair.first.carPlate
      this.driverName = pair.first.driverName
      this.location = pair.first.location
      this.describe = pair.first.describe
      this.hitForm = pair.first.hitForm
      this.hitType = pair.first.hitType
      this.source = pair.second.source
      this.authorId = pair.second.authorId
      this.authorName = pair.second.authorName
    }
    val user = randomAuthenticatedUser()
    val operation = Operation(
      time = OffsetDateTime.now(),
      type = Creation.name,
      target = Target(id = pair.first.id.toString(), type = ACCIDENT_DRAFT_TARGET_TYPE),
      operator = user.toOperator(),
      cluster = "$ACCIDENT_OPERATION_CLUSTER-${pair.first.id}",
      result = calculateOverdueDayAndHour(pair.first.happenTime!!,
        pair.second.draftTime!!, draftOverdueSeconds).let {
        if (it == "") CREATION_NOT_OVERDUE_RESUTLT
        else CREATION_OVERDUE_RESUTLT + it
      },
      title = operationTitles[OperationType.Creation.name + ACCIDENT_DRAFT_TARGET_TYPE]!!
    )
    `when`(accidentDao.verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)).thenReturn(Mono.empty())
    `when`(accidentDao.createCase(dto)).thenReturn(pair.toMono())
    `when`(securityService.getAuthenticatedUser()).thenReturn(Mono.just(Optional.of(user)))
    `when`(operationService.create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && result == operation.result && title == operation.title
    })).thenReturn(Mono.empty())

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual).expectNext(pair).verifyComplete()
    verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
    verify(accidentDao).verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)
    verify(accidentDao).createCase(dto)
    verify(securityService).getAuthenticatedUser()
    verify(operationService).create(argThat {
      !time.isBefore(operation.time) && type == operation.type && target == operation.target
        && operator == operation.operator && cluster == operation.cluster
        && result == operation.result && title == operation.title
    })
  }

  @Test
  fun `Failed by PermissionDenied`() {
    // mock
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT)).thenReturn(Mono.error(PermissionDeniedException()))
    val dto = AccidentDraftDto4FormSubmit().apply {
      carPlate = randomString("粤A.")
      happenTime = OffsetDateTime.now()
    }

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual)
      .expectError(PermissionDeniedException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
    verify(accidentDao, times(0)).verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)
    verify(accidentDao, times(0)).createCase(dto)
    verify(securityService, times(0)).getAuthenticatedUser()
    verify(operationService, times(0)).create(any())
  }

  @Test
  fun `Failed by NonUnique`() {
    // mock
    `when`(securityService.verifyHasAnyRole(ROLE_DRAFT_SUBMIT)).thenReturn(Mono.empty())
    val dto = AccidentDraftDto4FormSubmit().apply {
      carPlate = randomString("粤A.")
      happenTime = OffsetDateTime.now()
    }
    `when`(accidentDao.verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!))
      .thenReturn(Mono.error(NonUniqueException()))

    // invoke
    val actual = accidentDraftService.submit(dto)

    // verify
    StepVerifier.create(actual)
      .expectError(NonUniqueException::class.java)
      .verify()
    verify(securityService).verifyHasAnyRole(ROLE_DRAFT_SUBMIT)
    verify(accidentDao).verifyCaseNotExists(dto.carPlate!!, dto.happenTime!!)
    verify(accidentDao, times(0)).createCase(dto)
    verify(securityService, times(0)).getAuthenticatedUser()
    verify(operationService, times(0)).create(any())
  }
}