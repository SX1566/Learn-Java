package cn.gftaxi.traffic.accident.bc.dao.jdbc

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.LocalDate

/**
 * Test [BcDaoImpl.getMotorcadeName].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
class GetMotorcadeNameMethodImplTest @Autowired constructor(
  private val bcDao: BcDao
) {
  // 车号不存在
  @Test
  fun `Car NotExists`() {
    StepVerifier.create(bcDao.getMotorcadeName("UNKNOWN", LocalDate.now()))
      .expectNext("").verifyComplete()
  }

  // 车辆存在当前分配车队但没有转车队迁移记录
  @Test
  fun `Car Without History`() {
    StepVerifier.create(bcDao.getMotorcadeName("新车", LocalDate.now()))
      .expectNext("宝城").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName("新车", LocalDate.now().minusYears(1)))
      .expectNext("宝城").verifyComplete()
  }
}