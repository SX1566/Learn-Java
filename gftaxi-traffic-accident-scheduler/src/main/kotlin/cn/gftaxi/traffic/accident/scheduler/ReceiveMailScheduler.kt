package cn.gftaxi.traffic.accident.scheduler

import cn.gftaxi.traffic.accident.dao.AccidentDao
import cn.gftaxi.traffic.accident.dao.AccidentMailDao
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
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
  private val accidentDao: AccidentDao
) {
  private val logger = LoggerFactory.getLogger(ReceiveMailScheduler::class.java)

  @CronScheduled(cron = "\${app.cron.receive-mail: 0 0/30 * * * ? *}", group = "GFTAXI", name = "收取报案邮件")
  fun execute() {
    logger.debug("start receive mails")
    try {
      accidentMailDao.receiveMail()              // 收取邮件
        .flatMap { accidentDao.createCase(it) } // 创建案件
        .blockLast()
      logger.debug("end receive mails")
    } catch (e: Throwable) {
      logger.warn(e.message)
    }
  }
}