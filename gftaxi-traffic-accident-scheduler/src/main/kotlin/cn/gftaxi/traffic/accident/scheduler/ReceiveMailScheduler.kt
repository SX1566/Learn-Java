package cn.gftaxi.traffic.accident.scheduler

import cn.gftaxi.traffic.accident.dao.AccidentDraftDao
import cn.gftaxi.traffic.accident.dao.AccidentMailDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import cn.gftaxi.traffic.accident.po.AccidentDraft
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import tech.simter.scheduling.quartz.CronScheduled

/**
 * 收取报案邮件生成事故报案信息。
 *
 * 通过 `'app.cron.receive-mail'` 属性设置执行时点，默认为 `'0 0/30 * * * ? *'` 。
 *
 * @author RJ
 */
@Component
@Profile("scheduler")
class ReceiveMailScheduler @Autowired constructor(
  private val accidentMailDao: AccidentMailDao,
  private val accidentDraftDao: AccidentDraftDao,
  @Value("\${app.draft-overdue-hours: 12}") // 默认报案时限为12小时
  private val overdueHours: Long
) {
  private val logger = LoggerFactory.getLogger(ReceiveMailScheduler::class.java)
  val overdueSeconds = overdueHours * 60 * 60

  init {
    logger.warn("overdueHours=$overdueHours")
  }

  @CronScheduled(cron = "\${app.cron.receive-mail: 0 0/30 * * * ? *}", group = "GFTAXI", name = "收取报案邮件")
  fun execute() {
    try {
      accidentMailDao.receiveMail()          // 收取邮件
        .flatMap { accidentDraftDao.nextCode(it.happenTime!!).zipWith(Mono.just(it)) } // 生成事故编号
        .map { dto2Po(it.t1, it.t2) }        // DTO 转 PO
        .map { accidentDraftDao.create(it) } // 保存 PO
        .blockLast()
    } catch (e: Throwable) {
      logger.warn(e.message)
    }
  }

  private fun dto2Po(code: String, dto: AccidentDraftDto4Submit): AccidentDraft {
    return AccidentDraft(
      status = AccidentDraft.Status.Todo,
      code = code,
      carPlate = dto.carPlate!!,
      driverName = dto.driverName!!,
      happenTime = dto.happenTime!!,
      reportTime = dto.reportTime!!,
      location = dto.location!!,
      hitForm = dto.hitForm,
      hitType = dto.hitType,
      overdue = AccidentDraft.isOverdue(dto.happenTime!!, dto.reportTime!!, overdueSeconds),
      source = dto.source!!,
      authorName = dto.authorName!!,
      authorId = dto.authorId!!,
      describe = dto.describe
    )
  }
}