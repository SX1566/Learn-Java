package cn.gftaxi.traffic.accident

import cn.gftaxi.traffic.accident.Utils.polishCarPlate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test [Utils].
 *
 * @author RJ
 */
class UtilsTest {
  @Test
  fun polishCarPlate() {
    val expected = "粤A123456"
    assertEquals(expected, polishCarPlate("粤A.123456"))
    assertEquals(expected, polishCarPlate("粤A•123456"))
    assertEquals(expected, polishCarPlate("粤A・123456"))
    assertEquals(expected, polishCarPlate("粤A123456"))
    assertEquals("123456", polishCarPlate("123456"))
  }
}