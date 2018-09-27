package cn.gftaxi.traffic.accident.po

import cn.gftaxi.traffic.accident.po.base.AccidentCaseBase
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction.CASCADE
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.FetchType.LAZY

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
  @get:OneToMany(fetch = LAZY, cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @get:OrderBy("sn asc")
  var cars: List<AccidentCar>? by holder

  // 当事人列表
  @get:OnDelete(action = CASCADE)
  @get:OneToMany(fetch = LAZY, cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @get:OrderBy("sn asc")
  var peoples: List<AccidentPeople>? by holder

  // 其他物体列表
  @get:OnDelete(action = CASCADE)
  @get:OneToMany(fetch = LAZY, cascade = [ALL], orphanRemoval = true, mappedBy = "parent")
  @get:OrderBy("sn asc")
  var others: List<AccidentOther>? by holder
}