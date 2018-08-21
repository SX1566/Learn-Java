package cn.gftaxi.traffic.accident.dao.jdbc.bc

import cn.gftaxi.traffic.accident.dao.BcDao
import cn.gftaxi.traffic.accident.dao.jdbc.BcDaoImpl
import cn.gftaxi.traffic.accident.dao.jpa.ModuleConfiguration
import cn.gftaxi.traffic.accident.dto.CaseRelatedInfoDto
import cn.gftaxi.traffic.accident.po.AccidentRegister.DriverType.Official
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier
import java.time.LocalDate

/**
 * 用 U4A31 的实际数据测试一下 [BcDaoImpl.getCaseRelatedInfo]。
 *
 * 此车已注销，交车日期为 2018-02-12。
 *
 * ```
 * 劳动合同：1. 黄俊强 2013-02-25~2016-06-30 离职
 *           2. 黄俊权 2013-02-25~2016-06-30 注销
 *           3. 黄俊权 2016-06-30~2018-02-23 注销
 *           3. 黄俊强 2016-12-30~2018-02-12 离职
 * 迁移记录：1. 黄俊权 新入职       2013-02-26
 *           2. 黄俊强 新入职       2013-02-26
 *           3.        车队到车队   2013-05-09 四分一队转四分二队
 *           4.        车队到车队   2013-09-10 四分五队转四分二队
 *           5.        车队到车队   2013-12-01 四分二队转三分二队
 *           6. 黄俊强 交回未注销   2016-06-30
 *           7. 黄俊强 交回后转车   2016-12-31
 *           8.        车队到车队   2017-01-01 三分二队转二分二队
 *           9. 黄俊强 注销未有去向 2018-02-22
 *          10. 黄俊权 注销未有去向 2018-02-22
 * ```
 *
 * @author RJ
 */
@SpringJUnitConfig(ModuleConfiguration::class, BcDataSourceConfiguration::class)
@DataJpaTest
class GetCaseRelatedInfoMethodImplWithU4A31Test @Autowired constructor(
  private val bcDao: BcDao
) {
  companion object {
    // 黄俊强
    private val DRIVER_BASE_INFO = CaseRelatedInfoDto(
      driverId = 10258907,
      driverUid = "carMan.uid.484",
      driverPhone = "13710337359",
      driverHiredDate = LocalDate.of(2013, 2, 26),
      driverBirthDate = LocalDate.of(1979, 5, 19),
      driverIdentityCode = "441425197905195118",
      driverServiceCode = "245953",
      driverOrigin = "广东省/梅州市",
      driverLicenseDate = LocalDate.of(1996, 8, 18)
    )
    // U4A31
    private val CAR_BASE_INFO = CaseRelatedInfoDto(
      motorcadeName = "二分二队",
      contractType = "承包合同",
      carId = 10439021,
      carModel = "桑塔纳 SVW7182QQD",
      carOperateDate = LocalDate.of(2013, 2, 25)
    )
    // 混合
    private val CAR_DRIVER_BASE_INFO = CaseRelatedInfoDto(
      driverId = DRIVER_BASE_INFO.driverId,
      driverUid = DRIVER_BASE_INFO.driverUid,
      driverPhone = DRIVER_BASE_INFO.driverPhone,
      driverHiredDate = DRIVER_BASE_INFO.driverHiredDate,
      driverBirthDate = DRIVER_BASE_INFO.driverBirthDate,
      driverIdentityCode = DRIVER_BASE_INFO.driverIdentityCode,
      driverServiceCode = DRIVER_BASE_INFO.driverServiceCode,
      driverOrigin = DRIVER_BASE_INFO.driverOrigin,
      driverLicenseDate = DRIVER_BASE_INFO.driverLicenseDate,

      motorcadeName = CAR_BASE_INFO.motorcadeName,
      contractType = CAR_BASE_INFO.contractType,
      carId = CAR_BASE_INFO.carId,
      carModel = CAR_BASE_INFO.carModel,
      carOperateDate = CAR_BASE_INFO.carOperateDate
    )
  }

  private val carPlate = "U4A31"
  private val driverName = "黄俊强"

  // 车号不存在、司机存在
  @Test
  fun `1 Car NotExists But DriverExists`() {
    val carPlate = "UNKNOWN"

    val date = LocalDate.of(2016, 6, 30) // 交回未注销
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = date
    )).expectNext(DRIVER_BASE_INFO).verifyComplete()
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = date.minusDays(1)
    )).expectNext(DRIVER_BASE_INFO).verifyComplete()
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = date.plusDays(1)
    )).expectNext(DRIVER_BASE_INFO).verifyComplete()
  }

  @Test
  fun `2 2013-02-26 新入职前`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2013, 2, 26).minusDays(10)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      motorcadeName = "四分一队"
    )).verifyComplete()
  }

  @Test
  fun `3 2013-02-26 新入职当天`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2013, 2, 26)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      driverType = Official,
      motorcadeName = "四分一队",
      contractDrivers = "黄俊强, 黄俊权",

      // 有对班
      relatedDriverName = "黄俊权",
      relatedDriverPhone = "13538851358"
    )).verifyComplete()
  }

  @Test
  fun `4 2016-06-30 黄俊强 交回未注销`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2016, 6, 30)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      driverType = Official,
      motorcadeName = "三分二队",
      contractDrivers = "黄俊强, 黄俊权",

      // 有对班
      relatedDriverName = "黄俊权",
      relatedDriverPhone = "13538851358"
    )).verifyComplete()
  }

  @Test
  fun `5 2016-06-30 黄俊强 交回未注销 的第二天`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2016, 6, 30).plusDays(1)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      driverType = null,
      motorcadeName = "三分二队",
      contractDrivers = "黄俊权" // 单班营运
    )).verifyComplete()
  }

  @Test
  fun `6 2016-12-31 黄俊强 交回后转车 前`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2016, 12, 31).minusDays(2)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      driverType = null,
      motorcadeName = "三分二队",
      contractDrivers = "黄俊权" // 单班营运
    )).verifyComplete()
  }

  @Test
  fun `7 2016-12-31 黄俊强 交回后转车 当天`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2016, 12, 31)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      driverType = Official,
      motorcadeName = "三分二队",
      contractDrivers = "黄俊权, 黄俊强", // 恢复双班营运

      // 有对班
      relatedDriverName = "黄俊权",
      relatedDriverPhone = "13538851358"
    )).verifyComplete()
  }

  @Test
  fun `8 2018-02-22 黄俊强 注销未有去向 后`() {
    StepVerifier.create(bcDao.getCaseRelatedInfo(
      carPlate = carPlate,
      driverName = driverName,
      date = LocalDate.of(2018, 2, 22).plusDays(10)
    )).expectNext(CAR_DRIVER_BASE_INFO.copy(
      driverType = null,
      motorcadeName = "二分二队"
    )).verifyComplete()
  }
}