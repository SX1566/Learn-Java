package cn.gftaxi.traffic.accident.po

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故其他物体 PO。
 *
 * @author JF
 * @author RJ
 */
@Entity
@Table(
  name = "gf_accident_other",
  uniqueConstraints = [UniqueConstraint(columnNames = ["pid", "name"])]
)
data class AccidentOther constructor(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Int? = null,
  /** 所属事故 */
  @ManyToOne(optional = false)
  @JoinColumn(name = "pid", nullable = false)
  val parent: AccidentRegister,
  /** 同一事故内的序号 */
  val sn: Short,
  /** 名称 */
  @Column(length = 50)
  val name: String,
  /** 分类 */
  @Column(length = 50)
  val type: String,
  /** 归属 */
  @Column(length = 50)
  val belong: String? = null,
  /** 联系人 */
  @Column(length = 50)
  val linkmanName: String? = null,
  /** 联系电话 */
  @Column(length = 50)
  val linkmanPhone: String? = null,
  /** 受损情况 */
  @Column(length = 50)
  val damageState: String? = null,
  /** 损失预估（元） */
  @Column(precision = 10, scale = 2)
  val damageMoney: BigDecimal? = null,
  /** 实际损失（元） */
  @Column(precision = 10, scale = 2)
  val actualMoney: BigDecimal? = null,
  /** 跟进形式 */
  @Column(length = 50)
  val followType: String? = null,
  /** 更新时间 */
  val updatedTime: OffsetDateTime? = null
)