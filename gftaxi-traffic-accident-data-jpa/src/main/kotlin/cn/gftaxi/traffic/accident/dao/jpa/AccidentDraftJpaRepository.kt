package cn.gftaxi.traffic.accident.dao.jpa

import cn.gftaxi.traffic.accident.po.AccidentDraft
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 事故报案 Repository 接口。
 *
 * @author JF
 */
interface AccidentDraftJpaRepository : JpaRepository<AccidentDraft, String>