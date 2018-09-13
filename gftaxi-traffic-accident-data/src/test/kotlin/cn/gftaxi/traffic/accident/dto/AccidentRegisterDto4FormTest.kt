package cn.gftaxi.traffic.accident.dto

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccidentRegisterDto4FormTest {
  @Test
  fun `missing field`() {
    val json = "{}"
    val dto = ObjectMapper().readValue(json, AccidentRegisterDto4Form::class.java)
    assertTrue(dto.data.isEmpty())
    assertNull(dto.carPlate)
  }

  @Test
  fun `explicit set field`() {
    val json = "{\"carPlate\": null, \"carId\": 1, \"motorcadeName\": \"一分一队\"}"
    val dto = ObjectMapper().readValue(json, AccidentRegisterDto4Form::class.java)
    //println(dto)
    assertEquals(3, dto.data.size)
    assertNull(dto.carPlate)
    assertEquals(1, dto.carId)
    assertEquals("一分一队", dto.motorcadeName)
  }

  @Test
  @Disabled
  fun `explicit set happenTime`() {
    val json = "{\"happenTime\": \"2018-10-01 15:30\"}"
    val dto = ObjectMapper()
      .findAndRegisterModules()
      .readValue(json, AccidentRegisterDto4Form::class.java)
    //println(dto)
    assertEquals(1, dto.data.size)
    assertNull(dto.carPlate)
  }

  @Test
  @Disabled
  fun println() {
    println(LocalDateTime.parse("2018-10-01T15:30:20"))
    println(LocalDateTime.parse("2018-10-01T15:30"))
    println(OffsetDateTime.parse("2018-10-01T15:30:20+08:00"))
    println(OffsetDateTime.parse("2018-10-01T15:30+08:00"))
    println(OffsetDateTime.parse("2018-10-01T15:30:20Z"))
    println(OffsetDateTime.parse("2018-10-01T15:30Z"))
    println(OffsetDateTime.now()) // 2018-08-07T14:22:25.866+08:00
    println(OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)) // 2018-08-07T14:22+08:00
  }
}