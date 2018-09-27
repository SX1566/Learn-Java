package cn.gftaxi.traffic.accident.dto

import javax.persistence.MappedSuperclass

/**
 * 事故登记信息表单用 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentRegisterDto4FormUpdate : AccidentCaseDto4FormBase()