package cn.gftaxi.traffic.accident.po

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故当事人 PO。
 *
 * @author JF
 */
@Entity
@Table(name = "gf_accident_people")
data class AccidentPeople constructor(
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
  /** 姓名 */
  @Column(length = 50)
  val name: String,
  /** 性别 */
  val sex: Sex,
  /** 联系电话 */
  @Column(length = 50)
  val phone: String,
  /** 交通方式 */
  @Column(length = 50)
  val transportType: String,
  /** 事故责任 */
  @Column(length = 50)
  val duty: String,
  /** 人员情况 */
  @Column(length = 50)
  val personState: String,
  /** 伤亡情况 */
  @Column(length = 50)
  val damageState: String,
  /** 损失预估（元） */
  @Column(precision = 10, scale = 2)
  val damageMoney: BigDecimal,
  /** 医疗费用（元） */
  @Column(precision = 10, scale = 2)
  val treatmentMoney: BigDecimal,
  /** 赔偿损失（元） */
  @Column(precision = 10, scale = 2)
  val compensateMoney: BigDecimal,
  /** 跟进形式 */
  @Column(length = 50)
  val followType: String,
  /** 更新时间 */
  val updatedTime: OffsetDateTime
) {
  /**
   * 性别。
   */
  enum class Sex(private val value: Short) {
    /**
     * 未设置。
     */
    NotSet(0),
    /**
     * 男。
     */
    Male(1),
    /**
     * 女。
     */
    Female(2);

    fun value(): Short {
      return value
    }
  }
}