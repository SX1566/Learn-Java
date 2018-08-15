package cn.gftaxi.traffic.accident.dao.jpa.repository

import cn.gftaxi.traffic.accident.po.AccidentRegister
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 事故登记 Repository 接口。
 *
 * @author RJ
 */
interface AccidentRegisterJpaRepository : JpaRepository<AccidentRegister, Int>