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
 * 用 U4A31 的实际数据测试一下 [BcDaoImpl.getMotorcadeName]。
 *
 * 此车已注销，交车日期为 2018-02-12。
 *
 * ```
 * 迁移记录：1. 2013-05-09 四分一队转四分二队
 *           2. 2013-09-10 四分五队转四分二队
 *           3. 2013-12-01 四分二队转三分二队
 *           4. 2017-01-01 三分二队转二分二队
 * ```
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
class GetMotorcadeNameMethodImplWithU4A31Test @Autowired constructor(
  private val bcDao: BcDao
) {
  private val carPlate = "U4A31"

  @Test
  fun `1 2013-05-09 四分一队转四分二队 前`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2013, 5, 9).minusDays(1)))
      .expectNext("四分一队").verifyComplete()
  }

  @Test
  fun `2 2013-05-09 四分一队转四分二队`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2013, 5, 9)))
      .expectNext("四分二队").verifyComplete()
  }

  @Test
  fun `3 2013-12-01 四分二队转三分二队 前`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2013, 12, 1).minusDays(1)))
      .expectNext("四分二队").verifyComplete()
  }

  @Test
  fun `4 2013-12-01 四分二队转三分二队`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2013, 12, 1)))
      .expectNext("三分二队").verifyComplete()
  }

  @Test
  fun `5 2017-01-01 三分二队转二分二队 前`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2017, 1, 1).minusDays(1)))
      .expectNext("三分二队").verifyComplete()
  }

  @Test
  fun `6 2017-01-01 三分二队转二分二队`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2017, 1, 1)))
      .expectNext("二分二队").verifyComplete()
  }

  @Test
  fun `6 2017-01-01 三分二队转二分二队 后`() {
    StepVerifier.create(bcDao.getMotorcadeName(carPlate, LocalDate.of(2017, 1, 1).plusDays(10)))
      .expectNext("二分二队").verifyComplete()
  }
}