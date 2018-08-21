package cn.gftaxi.traffic.accident.dao.jdbc.bc

import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dao.jdbc.BcDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dto.CaseRelatedInfoDto
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.LocalDate

/**
 * Test [BcDaoImpl.getCaseRelatedInfo].
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class, BcDataSourceConfiguration::class)
@DataJpaTest
class GetCaseRelatedInfoMethodImplTest @Autowired constructor(
  private val bcDao: BcDao
) {
  @Test
  fun `Car And Driver Both Not Exists`() {
    // 车号司机都不存在
    val carPlate = "UNKNOWN"
    val driverName = "UNKNOWN"
    val date = LocalDate.now()
    val emptyDto = CaseRelatedInfoDto()
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = date
    )).expectNext(emptyDto).verifyComplete()
  }
}