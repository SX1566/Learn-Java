package cn.gftaxi.traffic.accident.po

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故当事车辆 PO。
 *
 * @author JF
 * @author RJ
 */
@Entity
@Table(
  name = "gf_accident_car",
  uniqueConstraints = [UniqueConstraint(columnNames = ["pid", "name"])]
)
data class AccidentCar constructor(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  override val id: Int? = null,
  /** 所属事故 */
  @ManyToOne(optional = false)
  @JoinColumn(name = "pid", nullable = false)
  val parent: AccidentRegister,
  /** 同一事故内的序号 */
  val sn: Short,
  /** 车号，如 粤A.123456 */
  @Column(length = 10)
  val name: String,
  /** 分类 */
  @Column(length = 50)
  val type: String,
  /** 车型：出租车、小轿车、... */
  @Column(length = 50)
  val model: String? = null,
  /** 拖车次数 */
  val towCount: Short? = null,
  /** 拖车费（元） */
  @Column(precision = 10, scale = 2)
  val towMoney: BigDecimal? = null,
  /** 维修分类：厂修、外修 */
  @Column(length = 50)
  val repairType: String? = null,
  /** 维修费（元） */
  @Column(precision = 10, scale = 2)
  val repairMoney: BigDecimal? = null,
  /** 受损情况 */
  @Column(length = 50)
  val damageState: String? = null,
  /** 损失预估（元） */
  @Column(precision = 10, scale = 2)
  val damageMoney: BigDecimal? = null,
  /** 跟进形式 */
  @Column(length = 50)
  val followType: String? = null,
  /** 更新时间 */
  val updatedTime: OffsetDateTime? = null
) : IdEntity