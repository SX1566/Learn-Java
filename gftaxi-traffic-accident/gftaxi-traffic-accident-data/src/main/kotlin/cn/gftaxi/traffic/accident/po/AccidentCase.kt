package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.common.FieldComment
import cn.gftaxi.traffic.accident.po.base.AccidentCaseBase
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction.CASCADE
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * 案件信息 PO。
 *
 * @author RJ
 */
@Entity
@Table(
  name = "gf_accident_case",
  uniqueConstraints = [UniqueConstraint(columnNames = ["carPlate", "happenTime"])]
)
class AccidentCase : AccidentCaseBase() {
  // 当事车辆列表
  @get:OnDelete(action = CASCADE) // 加上这个自动建表语句才会有 ON DELETE CASCADE
  @get:OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @get:OrderBy("sn asc")
  @get:LazyCollection(LazyCollectionOption.FALSE)
  var cars: List<AccidentCar>? by holder

  // 当事人列表
  @get:OnDelete(action = CASCADE)
  @get:OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @get:OrderBy("sn asc")
  @get:LazyCollection(LazyCollectionOption.FALSE)
  var peoples: List<AccidentPeople>? by holder

  // 其他物体列表
  @get:OnDelete(action = CASCADE)
  @get:OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @get:OrderBy("sn asc")
  @get:LazyCollection(LazyCollectionOption.FALSE)
  var others: List<AccidentOther>? by holder

  companion object {
    private val properties = AccidentCase::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
    val comments = properties.map { it.name to it.findAnnotation<FieldComment>()?.comment }.associate { it }
    val propertieTypes: Map<String, KClass<*>> = properties.map { it.name to it.returnType.jvmErasure }.associate { it }
    val propertieKeys = properties.map { it.name }
  }
}