package cn.gftaxi.traffic.accident.dao.jpa.repository

import cn.gftaxi.traffic.accident.po.AccidentOperation
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 事故操作记录 Repository 接口。
 *
 * @author RJ
 */
interface AccidentOperationJpaRepository : JpaRepository<AccidentOperation, Int>