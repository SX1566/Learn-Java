package cn.gftaxi.traffic.accident.dto

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.time.temporal.ChronoUnit

class AccidentRegisterDto4FormTest {
  private val objectMapper = ObjectMapper().registerModule(tech.simter.jackson.ext.javatime.JavaTimeModule())

  @Test
  fun `Empty JSON`() {
    val json = "{}"
    val dto = objectMapper.readValue(json, AccidentRegisterDto4Form::class.java)
    assertTrue(dto.data.isEmpty())
    assertNull(dto.carPlate)
  }

  @Test
  fun `Explicit set field`() {
    val json = "{\"carPlate\": null, \"carId\": 1, \"motorcadeName\": \"一分一队\"}"
    val dto = objectMapper.readValue(json, AccidentRegisterDto4Form::class.java)
    //println(dto)
    assertEquals(3, dto.data.size)
    assertNull(dto.carPlate)
    assertTrue(dto.data.containsKey("carPlate"))
    assertEquals(1, dto.carId)
    assertEquals("一分一队", dto.motorcadeName)
  }

  @Test
  fun `Explicit set happenTime`() {
    val happenTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val json = "{\"happenTime\": \"${happenTime.format(ofPattern("yyyy-MM-dd HH:mm"))}\"}"
    val dto = objectMapper.readValue(json, AccidentRegisterDto4Form::class.java)
    assertEquals(1, dto.data.size)
    assertNull(dto.carPlate)
    assertTrue(dto.data.containsKey("happenTime"))
    assertEquals(happenTime, dto.happenTime)
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