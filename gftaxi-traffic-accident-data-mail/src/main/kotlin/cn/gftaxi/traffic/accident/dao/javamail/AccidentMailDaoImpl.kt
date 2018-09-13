package cn.gftaxi.traffic.accident.dao.javamail

import cn.gftaxi.traffic.accident.Utils.FORMAT_DATE_TIME_TO_MINUTE
import cn.gftaxi.traffic.accident.dao.AccidentMailDao
import cn.gftaxi.traffic.accident.dto.AccidentDraftDto4Submit
import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPMessage
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.io.File
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.search.ComparisonTerm.GE
import javax.mail.search.SentDateTerm
import kotlin.collections.HashMap


/**
 * 事故邮件 Dao 的 JavaMail 实现。
 * @author RJ
 */
@Component
class AccidentMailDaoImpl @Autowired constructor(
  @Value("\${app.mail.imap-host:imap.139.com}")
  private val mailHost: String,
  @Value("\${app.mail.imap-timeout:25000}") // default 25s
  private val mailTimeout: Long,
  @Value("\${app.mail.username:gftaxi}") // gftaxi@139.com
  private val mailUsername: String,
  @Value("\${app.mail.password:gf81800088}")
  private val mailPassword: String,
  @Value("\${app.mail.receive-subject:BC事故报案}") // 报案邮件的标题
  private val receiveMailSubject: String,
  @Value("\${app.mail.receive-last-minutes:60}")   // default 1 hour
  private val receiveLastMinutes: Long,
  @Value("\${app.mail.mail-id-file:/data/accident/mail-id.txt}")   // mail-id cache file
  private val mailIdCacheFile: String
) : AccidentMailDao {
  private val logger: Logger = LoggerFactory.getLogger(AccidentMailDaoImpl::class.java)
  private val storedUIDs = UIDStore(File(mailIdCacheFile))

  init {
    logger.warn("app.mail.imap-host=$mailHost")
    logger.warn("app.mail.imap-timeout=$mailTimeout")
    logger.warn("app.mail.username=$mailUsername")
    logger.warn("app.mail.password=$mailPassword")
    logger.warn("app.mail.receive-subject=$receiveMailSubject")
    logger.warn("app.mail.receive-last-minutes=$receiveLastMinutes")
    logger.warn("storedUIDs.count=${storedUIDs.size}")
  }

  override fun receiveMail(): Flux<AccidentDraftDto4Submit> {
    return Flux.fromIterable(receiveMail(
      host = mailHost,
      username = mailUsername,
      password = mailPassword
    ))
  }

  fun receiveMail(host: String, username: String, password: String): List<AccidentDraftDto4Submit> {
    // 准备连接服务器的会话信息
    val props = Properties()
    props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.setProperty("mail.imap.socketFactory.fallback", "false")
    props.setProperty("mail.imap.socketFactory.port", "993") // 邮件服务器端口号
    props.setProperty("mail.imap.host", host)                // 邮件服务器
    props.setProperty("mail.imap.ssl.enable", "true")
    props.setProperty("mail.imap.timeout", mailTimeout.toString())
    props.setProperty("mail.imaps.timeout", props.getProperty("mail.imap.timeout"))

    // 创建Session实例对象
    val session = Session.getInstance(props)

    // 创建IMAP协议的Store对象
    val store = session.getStore("imaps")
    store.connect(host, username, password)

    // 获得收件箱
    val folder = store.getFolder("INBOX") as IMAPFolder

    // 打开收件箱
    folder.open(Folder.READ_ONLY)

    // 获得收件箱中邮件 （不要使用 [SubjectTerm] 条件，实测奇慢）
    // 服务器最近 N 分钟收取的邮件
    val minInstant = OffsetDateTime.now().minusMinutes(receiveLastMinutes).toInstant()
    logger.debug("minInstant=$minInstant")
    val lastNMinutesTerm = SentDateTerm(GE, Date.from(minInstant))

    // 获取邮件
    val messages = folder.search(lastNMinutesTerm)
    logger.info("all receive mail count=${messages.size}")

    // 解析邮件
    val result = messages
      // 过滤已经收取过的邮件
      .map { it as IMAPMessage }
      .filter {
        val isNew = !storedUIDs.contains(it.messageID)
        if (isNew) storedUIDs.add(it.messageID)
        isNew
      }
      // 再次过滤邮件时间，因为 SentDateTerm 过滤得不彻底
      .filter { minInstant.isBefore(it.sentDate.toInstant()) }
      // 过滤邮件标题
      .filter { it.subject.contains(receiveMailSubject) }
      // 转换为 dto
      .map { message2Dto(it) }

    logger.info("match mail result size=${result.size}")

    storedUIDs.store()
    folder.close(false)
    store.close()
    return result
  }

  private fun message2Dto(message: Message): AccidentDraftDto4Submit {
    val sender = message.from[0] as InternetAddress

    // 解析邮件内容
    val content = getText(message)

    if (logger.isDebugEnabled) {
      logger.debug("---------------------------------")
      logger.debug("mail.sentDate=${message.sentDate}")
      logger.debug("mail.receivedDate=${message.receivedDate}")
      logger.debug("mail.subject=${message.subject}")
      logger.debug("mail.content=$content")
      message.allHeaders.iterator().forEach { logger.debug("mail.header.${it.name}=${it.value}") }
    }

    // 按邮件模板格式解析邮件信息
    val map = analyticContent(content)

    // 修改邮件服务器的邮件状态，把邮件标记为已读
    //message.setFlag(Flags.Flag.SEEN, true)

    return AccidentDraftDto4Submit().apply {
      source = "MAIL"              // 邮件报案
      authorName = sender.personal ?: "" // 发送人昵称
      authorId = sender.address    // 发送人邮箱
      draftTime = OffsetDateTime.ofInstant(message.sentDate!!.toInstant(), ZoneOffset.systemDefault()) // 发送时间

      describe = content // 邮件内容

      carPlate = map["carPlate"] as String
      driverName = map["driverName"] as String
      happenTime = OffsetDateTime.of(
        LocalDateTime.parse(map["happenTime"] as String, FORMAT_DATE_TIME_TO_MINUTE),
        OffsetDateTime.now().offset
      )
      location = map["location"] as String
      hitForm = map["hitForm"] as String
      hitType = map["hitType"] as String
    }
  }

  fun analyticContent(content: String?): Map<String, String> {
    val map = HashMap<String, String>()
    if (content == null) return map

    // 按邮件模板要求拆分各个项目
    val items = content.trim().split("，", ",", "。")
    items.forEach {
      val kv = it.split("：", ":").map { it.trim() }
      if (logger.isDebugEnabled) logger.debug(kv.toString())
      when (kv[0]) {
      // standard
        "事故车号" -> map["carPlate"] = kv[1]
        "当事司机" -> map["driverName"] = kv[1]
        "事发时间" -> map["happenTime"] = "${kv[1]}:${kv[2]}"
        "事发地点" -> map["location"] = kv[1]
        "事故形态" -> map["hitForm"] = kv[1]
        "碰撞类型" -> map["hitType"] = kv[1]

      // same
        "事故发生时间" -> map["happenTime"] = "${kv[1]}:${kv[2]}"
        "事故发生地点" -> map["location"] = kv[1]
        "事故类型" -> map["hitForm"] = kv[1]
      }
    }

    if (logger.isDebugEnabled) logger.debug(map.toString())

    return map
  }

  // 解析邮件内容
  private fun getText(p: Part): String? {
    val content = when {
      p.isMimeType("text/*") -> p.content as String
      p.isMimeType("multipart/alternative") -> {
        // prefer html text over plain text
        val mp = p.content as Multipart
        var text: String? = null
        for (i in 0 until mp.count) {
          val bp = mp.getBodyPart(i)
          if (bp.isMimeType("text/plain")) {
            if (text == null) text = getText(bp)
          } else if (bp.isMimeType("text/html")) {
            val s = getText(bp)
            if (s != null) return s
          } else return getText(bp)
        }
        return text
      }
      p.isMimeType("multipart/*") -> {
        val mp = p.content as Multipart
        for (i in 0 until mp.count) {
          val bp = mp.getBodyPart(i)
          val s = getText(bp)
          if (s != null) return s
        }
        return null
      }
      else -> null
    }

    return Jsoup.parse(content).text().trim()
  }
}