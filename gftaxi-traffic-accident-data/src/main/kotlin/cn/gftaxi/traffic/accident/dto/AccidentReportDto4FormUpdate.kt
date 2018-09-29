package cn.gftaxi.traffic.accident.dto

import javax.persistence.MappedSuperclass

/**
 * 事故报告表单信息更新用 DTO。
 *
 * @author RJ
 */
@MappedSuperclass
open class AccidentReportDto4FormUpdate : AccidentCaseDto4FormBase()