package cn.gftaxi.traffic.accident.dao.jdbc.bc

import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dao.jdbc.BcDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.LocalDate

/**
 * Test [BcDaoImpl.getMotorcadeName].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class, BcDataSourceConfiguration::class)
@DataJpaTest
class GetMotorcadeNameMethodImplTest @Autowired constructor(
  private val bcDao: BcDao
) {
  @Test
  fun withNotExistsCar() {
    // 车号不存在
    StepVerifier.create(bcDao.getMotorcadeName("A1A1A1", LocalDate.now()))
      .expectNext("").verifyComplete()
  }

  @Test
  fun withoutHistoryCar() {
    // 车辆存在当前分配车队但没有转车队迁移记录
    StepVerifier.create(bcDao.getMotorcadeName("新车", LocalDate.now()))
      .expectNext("宝城").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName("新车", LocalDate.now().minusYears(1)))
      .expectNext("宝城").verifyComplete()
  }

  @Test
  fun withOneHistoryCar() {
    // 车辆存在当前分配车队且有 1 条转车队迁移记录
    // Z7J45 2013-12-01 转到 一分二队
    val baseDate = LocalDate.of(2013, 12, 1)
    val plate = "Z7J45"
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.minusDays(1)))
      .expectNext("一分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate))
      .expectNext("一分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.plusDays(1)))
      .expectNext("一分二队").verifyComplete()
  }

  @Test
  fun withMultiHistoryCar() {
    // 车辆存在当前分配车队且有 3 条转车队迁移记录
    //    U4A31;2013-12-01;三分二队
    //    U4A31;2013-09-10;四分二队
    //    U4A31;2013-05-09;四分二队
    val plate = "U4A31"

    var baseDate = LocalDate.of(2013, 5, 9)
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.minusDays(1)))
      .expectNext("四分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate))
      .expectNext("四分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.plusDays(1)))
      .expectNext("四分二队").verifyComplete()

    baseDate = LocalDate.of(2013, 9, 10)
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.minusDays(1)))
      .expectNext("四分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate))
      .expectNext("四分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.plusDays(1)))
      .expectNext("四分二队").verifyComplete()

    baseDate = LocalDate.of(2013, 12, 1)
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.minusDays(1)))
      .expectNext("四分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate))
      .expectNext("三分二队").verifyComplete()
    StepVerifier.create(bcDao.getMotorcadeName(plate, baseDate.plusDays(1)))
      .expectNext("三分二队").verifyComplete()
  }
}