package cn.gftaxi.traffic.accident.po

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故当事车辆 PO。
 *
 * @author JF
 */
@Entity
@Table(name = "gf_accident_car")
data class AccidentCar constructor(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Int?,
  /** 所属事故ID */
  @ManyToOne
  @JoinColumn(name = "id")
  val pid: AccidentRegister,
  /** 同一事故内的序号 */
  val sn: Short,
  /** 车辆分类：自车、三者 */
  @Column(length = 50)
  val type: String,
  /** 车号，如 粤A123456 */
  @Column(length = 8)
  val carPlate: String,
  /** 车型：出租车、小轿车、... */
  @Column(length = 50)
  val carType: String,
  /** 拖车次数 */
  val towCount: Short,
  /** 拖车费（元） */
  @Column(precision = 10, scale = 2)
  val towMoney: BigDecimal,
  /** 维修分类：厂修、外修 */
  @Column(length = 50)
  val repairType: String,
  /** 维修费（元） */
  @Column(precision = 10, scale = 2)
  val repairMoney: BigDecimal,
  /** 受损情况 */
  @Column(length = 50)
  val damageState: String,
  /** 损失预估（元） */
  @Column(precision = 10, scale = 2)
  val damageMoney: BigDecimal,
  /** 跟进形式 */
  @Column(length = 50)
  val followType: String,
  /** 更新时间 */
  val updatedTime: OffsetDateTime
)