package cn.gftaxi.traffic.accident.dto

import javax.persistence.MappedSuperclass

/**
 * 维修表单更新用 DTO
 *
 * @author SX
 */
@MappedSuperclass
open class RepairDto4FormUpdate : AccidentCaseDto4FormBase(){
    var source: String? by holder
    var authorName: String? by holder
    var authorId: String? by holder
}
