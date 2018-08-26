package cn.gftaxi.traffic.accident.dao.jpa.register

import cn.gftaxi.traffic.accident.dto.AccidentRegisterDto4Update
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties
import kotlin.test.assertNotNull

/**
 * 检测 [AccidentRegisterDto4Update] 的属性定义是否正确、完整。
 *
 * @author RJ
 */
class Dto4UpdateTest {
  @Test
  fun test() {
    val poProperties = AccidentRegisterDto4Update::class.memberProperties
    val dtoProperties = AccidentRegisterDto4Update::class.memberProperties
    val notInDtoProperties = listOf("id", "registerTime", "overdue")
    val nestedPropertyKeys = listOf("cars", "peoples", "others")

    // po 中可更新的属性应该在 dto 中都有定义
    poProperties.filter { !notInDtoProperties.contains(it.name) }.forEach { poProperty ->
      assertTrue(dtoProperties.any { dtoProperty -> poProperty.name == dtoProperty.name },
        "PO property '${poProperty.name}' is not in Dto")
    }

    // dto 中要更新的主体属性的类型应该与 po 中的一样
    dtoProperties.filter { !nestedPropertyKeys.contains(it.name) }.forEach { dtoProperty ->
      val poProperty = poProperties.firstOrNull { poProperty -> poProperty.name == dtoProperty.name }
      assertNotNull(poProperty)
      assertEquals(poProperty!!.returnType, dtoProperty.returnType)
    }
  }
}