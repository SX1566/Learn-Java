package cn.gftaxi.traffic.accident.common

/**
 * 注解在 Po 或 Dto 的字段上，用于运行时通过反射获得字段的描述
 *
 * @author zh
 */
@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class FieldComment(val comment: String = "")