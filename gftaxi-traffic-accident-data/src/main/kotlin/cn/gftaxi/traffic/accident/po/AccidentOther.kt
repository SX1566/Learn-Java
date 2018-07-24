package cn.gftaxi.traffic.accident.po

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故其他物体 PO。
 *
 * @author JF
 */
@Entity
@Table(name = "gf_accident_other")
data class AccidentOther constructor(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Int?,
  /** 所属事故ID */
  @ManyToOne(cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "pid")
  val parent: AccidentRegister,
  /** 同一事故内的序号 */
  val sn: Short,
  /** 物品分类 */
  @Column(length = 50)
  val type: String,
  /** 物品名称 */
  @Column(length = 50)
  val name: String,
  /** 归属 */
  @Column(length = 50)
  val belong: String?,
  /** 联系人 */
  @Column(length = 50)
  val linkmanName: String?,
  /** 联系电话 */
  @Column(length = 50)
  val linkmanPhone: String?,
  /** 受损情况 */
  @Column(length = 50)
  val damageState: String?,
  /** 损失预估（元） */
  @Column(precision = 10, scale = 2)
  val damageMoney: BigDecimal?,
  /** 实际损失（元） */
  @Column(precision = 10, scale = 2)
  val actualMoney: BigDecimal?,
  /** 跟进形式 */
  @Column(length = 50)
  val followType: String?,
  /** 更新时间 */
  val updatedTime: OffsetDateTime
)