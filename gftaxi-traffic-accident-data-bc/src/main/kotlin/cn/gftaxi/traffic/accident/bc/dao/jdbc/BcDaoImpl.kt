package cn.gftaxi.traffic.accident.bc.dao.jdbc

import cn.gftaxi.traffic.accident.bc.dao.BcDao
import cn.gftaxi.traffic.accident.bc.dto.CaseRelatedInfoDto
import cn.gftaxi.traffic.accident.common.DriverType
import cn.gftaxi.traffic.accident.common.DriverType.*
import cn.gftaxi.traffic.accident.common.Utils.polishCarPlate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.sql.Date
import java.time.LocalDate
import javax.sql.DataSource

/**
 * BC 系统相关信息 Dao 的实现。
 *
 * @author RJ
 */
@Component
class BcDaoImpl @Autowired constructor(
  @Qualifier("bcDataSource") private val dataSource: DataSource
) : BcDao {
  private val logger = LoggerFactory.getLogger(BcDaoImpl::class.java)
  private var jdbc: NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

  private fun getMotorcadeNameBlock(carPlate: String, date: LocalDate): String? {
    val sql = """
      select m.name
      from bs_car c, car__find_history_company_and_motorcade(c.id, :date) cm
      inner join bs_motorcade m on m.id = cm.motorcade_id
      where (c.plate_no = :plate or (concat(c.plate_type, c.plate_no) = :plate))
      """.trimIndent()
    return try {
      jdbc.queryForObject(sql, mapOf(
        "plate" to carPlate,
        "date" to java.sql.Date.valueOf(date)
      ), String::class.java)
    } catch (e: EmptyResultDataAccessException) {
      null
    }
  }

  override fun getMotorcadeName(carPlate: String, date: LocalDate): Mono<String> {
    val motorcadeName = getMotorcadeNameBlock(polishCarPlate(carPlate), date)
    return if (motorcadeName == null) Mono.just("") else Mono.just(motorcadeName)
  }

  override fun getCaseRelatedInfo(carPlate: String, driverName: String, date: LocalDate): Mono<CaseRelatedInfoDto> {
    val plate = polishCarPlate(carPlate)
    // 1. 获取车辆信息
    val car: Map<String, Any>? = try {
      jdbc.queryForMap("""
        select c.id as id, concat(c.plate_type, c.plate_no) as plate, c.bs_type as contract_type
          , concat(c.factory_type, ' ', c.factory_model) as model, cast(c.operate_date as date) as operate_date
          , m.name as motorcade_name
        from bs_car c
        inner join bs_motorcade m on m.id = c.motorcade_id
        where c.plate_no = :carPlate
        or concat(c.plate_type, c.plate_no) = :carPlate
      """.trimIndent(), mapOf("carPlate" to plate))
    } catch (e: EmptyResultDataAccessException) {
      null
    }
    logger.debug("car={}", car)

    // 车队信息
    val motorcadeName: String? = car?.let { getMotorcadeNameBlock(plate, date) }
      ?: car?.get("motorcadeName") as String?
    logger.debug("motorcadeName={}", motorcadeName)

    // 2. 获取车辆在指定日期的劳动合同 - 用于获取承包司机信息
    // contract_id, driver_id, from_date, to_date
    // from_date~to_date 为合同实际期限
    val laborContracts: List<Map<String, Any>> = car?.let {
      jdbc.queryForList("""
        -- 与车辆相关的所有劳动合同: from_date~to_date 为合同实际期限
        with contract1(driver_id, from_date, to_date, contract_type, driver_name, driver_phone) as (
          select dc.man_id, c.start_date::date
            , (case when c.status_ = 1 then coalesce(cl.leavedate::date, c.stop_date::date, cl.actual_end_date::date, end_date::date) -- 注销
                    when c.status_ = 2 then coalesce(cl.leavedate::date, c.stop_date::date, cl.actual_end_date::date, end_date::date) -- 离职
                    else end_date::date end) to_date -- 在案
            , cl.bs_type, d.name, d.phone
          from bs_car_contract cc
          inner join bs_contract c on c.id = cc.contract_id
          inner join bs_contract_labour cl on cl.id = c.id -- 劳动合同
          inner join bs_carman_contract dc on dc.contract_id = c.id
          inner join bs_carman d on d.id = dc.man_id
          where cc.car_id = :carId
          and c.status_ >= 0 -- 排除草稿合同
          order by c.start_date, dc.man_id
        )
        -- 合同期含指定日期的劳动合同
        , contract2(driver_id, from_date, to_date, contract_type, driver_name, driver_phone) as (
          select * from contract1 c where :theDate between from_date and to_date
        )
        -- 排除相同司机日期连续的较新的那份合同 (优先选择日期较早的合同)
        , contract(driver_id, from_date, to_date, contract_type, driver_name, driver_phone) as (
          select * from contract2 c where not exists (
            select 0 from contract2 c1
            where c.driver_id = c1.driver_id and c.from_date = c1.to_date
          )
        ) select * from contract c
      """.trimIndent(), mapOf("carId" to car["id"], "theDate" to date))
    } ?: listOf()
    logger.debug("laborContracts={}", laborContracts)

    // 3. 根据当事司机姓名获取司机信息
    //    如果 drivers 为 0 个，代表非编司机
    //    如果 drivers 只有 1 个，通过迁移记录获取营运班次信息
    //    如果 drivers 多于 1 个，需通过车辆劳动合同 + date 进行过滤
    var makers: List<Map<String, Any>> =
      jdbc.queryForList("""
        select id, uid_ as uid, name as name, phone, cast(birthdate as date) as birth_date
          , cast(work_date as date) as hired_date, cert_identity as identity_code, cert_fwzg as service_code
          , cast(cert_driving_first_date as date) as license_date, origin as origin
        from bs_carman where name = :driverName
      """.trimIndent(), mapOf("driverName" to driverName))
    logger.debug("makers-origin={}", makers)
    if (makers.size > 1) { // 多于 1 个，通过劳动合同 进行过滤
      makers = makers.filter { driver -> laborContracts.any { it["driver_id"] == driver["id"] } }
      if (makers.size > 1) return Mono.error(IllegalStateException("不应找到多个匹配的当事司机！"))
      logger.debug("makers-filter={}", makers)
    }
    // 当事司机
    val maker: Map<String, Any>? = makers.firstOrNull()
    // 对班司机 ID、电话：当事司机存在且其有相应的劳动合同时才有可能有对班
    val relatedDriver = if (maker != null && laborContracts.any { it["driver_id"] == maker["id"] })
      laborContracts.find { it["driver_id"] != maker["id"] }
        ?.let { mapOf("id" to it["driver_id"], "name" to it["driver_name"], "phone" to it["driver_phone"]) }
    else null
    logger.debug("maker={}, relatedDriver={}", maker, relatedDriver)

    // 4. 根据迁移记录获取司机的营运班次和车队信息
    val mm: Map<String, Any?>? = car?.let { _ ->
      maker?.let {
        try {
          // 司机对指定车辆在指定日的迁移记录状态
          jdbc.queryForMap("""
            select h.move_date as move_date, t.name_ move_name, t.working working
            from car_by_driver_history__find_current_ids(:driverId, :theDate, :carId) as ch
            inner join bs_car_driver_history h on h.id = ch.id
            inner join bs_car_driver_history_type t on t.id = h.move_type
            --where t.working = true -- 只需查在运状态的迁移记录
            where t.code != 'CZHB'   -- 排除重组合并
            order by h.move_date desc
            limit 1
            """.trimIndent(),
            mapOf("carId" to car["id"], "driverId" to maker["id"], "theDate" to date))
        } catch (e: EmptyResultDataAccessException) {
          null
        }
      }
    }
    logger.debug("mm={}", mm)
    val driverType: DriverType? = when (car) {
      null -> null
      else -> when (maker) {
        null -> Outside
        else -> when (mm) {
          null -> null
          else -> with(mm) {
            val moveDate = mm["move_date"] as java.sql.Date
            val working = mm["working"] as Boolean
            if (!working && moveDate < java.sql.Date.valueOf(date)) null
            else {
              if ((mm["move_name"] as String).startsWith("替班")) Shift else Official
            }
          }
        }
      }
    }

    // 5. 一年内的统计信息 TODO

    // 6. 合并信息返回
    val dto = CaseRelatedInfoDto(
      motorcadeName = motorcadeName,
      contractType = car?.get("contract_type") as String?,
      contractDrivers = if (laborContracts.isEmpty()) null
      else laborContracts.joinToString(", ") { it["driver_name"] as String },

      // 车辆
      carId = car?.get("id") as Int?,
      carModel = car?.get("model") as String?,
      carOperateDate = (car?.get("operate_date") as Date?)?.toLocalDate(),

      // 司机
      driverId = maker?.get("id") as Int?,
      driverUid = maker?.get("uid") as String?,
      driverType = driverType,
      driverPhone = maker?.get("phone") as String?,
      driverHiredDate = (maker?.get("hired_date") as Date?)?.toLocalDate(),
      driverBirthDate = (maker?.get("birth_date") as Date?)?.toLocalDate(),
      driverLicenseDate = (maker?.get("license_date") as Date?)?.toLocalDate(),
      driverIdentityCode = maker?.get("identity_code") as String?,
      driverServiceCode = maker?.get("service_code") as String?,
      driverOrigin = maker?.get("origin") as String?,
      relatedDriverName = relatedDriver?.get("name") as String?,
      relatedDriverPhone = relatedDriver?.get("phone") as String?,

      // 历史统计
      historyAccidentCount = null,
      historyTrafficOffenceCount = null,
      historyServiceOffenceCount = null,
      historyComplainCount = null

    )
    return Mono.just(dto)
  }
}