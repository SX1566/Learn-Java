package cn.gftaxi.traffic.accident.dao.javamail

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import kotlin.test.assertEquals

@SpringJUnitConfig(AccidentMailDaoImpl::class)
class AccidentMailDaoImplTest @Autowired constructor(
  private val mailDao: AccidentMailDaoImpl
) {

  @Test
  fun receiveMail() {
    mailDao.receiveMail().blockLast()
  }

  @Test
  fun analyticContent() {
    val content = "事故车号：Z0F41，当事司机：黄新，事故发生时间：2018-04-18 08:05，事故发生地点：荔湾区花地大道北路段，事故类型：车辆间事故，碰撞类型：追尾碰撞。"
    val map = mailDao.analyticContent(content)
    assertEquals("Z0F41", map["carPlate"])
    assertEquals("黄新", map["driverName"])
    assertEquals("荔湾区花地大道北路段", map["location"])
    assertEquals("车辆间事故", map["hitForm"])
    assertEquals("追尾碰撞", map["hitType"])
    assertEquals("2018-04-18 08:05", map["happenTime"])
  }
}