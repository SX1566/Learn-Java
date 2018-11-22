package cn.gftaxi.traffic.accident.dto

import javax.persistence.MappedSuperclass

/**
 * 事故登记表单信息更新用 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentRegisterDto4FormUpdate : AccidentCaseDto4FormBase()