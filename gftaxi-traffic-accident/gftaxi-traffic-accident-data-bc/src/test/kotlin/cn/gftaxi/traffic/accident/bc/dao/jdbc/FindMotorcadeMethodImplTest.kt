package cn.gftaxi.traffic.accident.bc.dao.jdbc

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.common.MotorcadeStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier

/**
 * [BcDaoImpl.findMotorcade] 单元测试。
 *
 * @author jw
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
class FindMotorcadeMethodImplTest @Autowired constructor(
  private val bcDao: BcDao
) {
  @Test
  fun findIncludeDisabled() {
    StepVerifier.create(bcDao.findMotorcade(true).collectList())
      .consumeNextWith {
        assertFalse(it.isEmpty())
        assertTrue(it.any { it.status === MotorcadeStatus.Enabled })
        assertTrue(it.any { it.status === MotorcadeStatus.Disabled })
      }.verifyComplete()
  }

  @Test
  fun findNotIncludeDisabled() {
    StepVerifier.create(bcDao.findMotorcade(false).collectList())
      .consumeNextWith {
        assertFalse(it.isEmpty())
        assertTrue(it.any { it.status === MotorcadeStatus.Enabled })
        assertTrue(it.all { it.status !== MotorcadeStatus.Disabled })
      }.verifyComplete()
  }
}