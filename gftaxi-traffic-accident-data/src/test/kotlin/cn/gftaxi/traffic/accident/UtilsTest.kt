package cn.gftaxi.traffic.accident

import cn.gftaxi.traffic.accident.Utils.calculateYears
import cn.gftaxi.traffic.accident.Utils.polishCarPlate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

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

  @Test
  fun calculateYears() {
    assertEquals(0, calculateYears(LocalDate.of(2018, 6, 2), LocalDate.of(2018, 6, 1)))
    assertEquals(0, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2018, 6, 1)))
    assertEquals(0, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2018, 7, 1)))
    assertEquals(0, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2018, 12, 31)))
    assertEquals(0, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2019, 5, 31)))
    assertEquals(1, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2019, 6, 1)))
    assertEquals(1, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2019, 12, 31)))
    assertEquals(1, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2020, 5, 31)))
    assertEquals(2, calculateYears(LocalDate.of(2018, 6, 1), LocalDate.of(2020, 6, 1)))
  }
}