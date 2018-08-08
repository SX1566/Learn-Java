package cn.gftaxi.traffic.accident.po

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.*

/**
 * 事故当事人 PO。
 *
 * @author JF
 * @author RJ
 */
@Entity
@Table(
  name = "gf_accident_people",
  uniqueConstraints = [UniqueConstraint(columnNames = ["pid", "name"])]
)
data class AccidentPeople constructor(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Int?,
  /** 所属事故 */
  @ManyToOne(optional = false)
  @JoinColumn(name = "pid", nullable = false)
  val parent: AccidentRegister,
  /** 同一事故内的序号 */
  val sn: Short,
  /** 姓名 */
  @Column(length = 50)
  val name: String,
  /** 分类 */
  @Column(length = 50)
  val type: String? = null,
  /** 性别 */
  val sex: Sex = Sex.NotSet,
  /** 联系电话 */
  @Column(length = 50)
  val phone: String? = null,
  /** 交通方式 */
  @Column(length = 50)
  val transportType: String? = null,
  /** 事故责任 */
  @Column(length = 50)
  val duty: String? = null,
  /** 伤亡情况 */
  @Column(length = 50)
  val damageState: String? = null,
  /** 损失预估（元） */
  @Column(precision = 10, scale = 2)
  val damageMoney: BigDecimal? = null,
  /** 医疗费用（元） */
  @Column(precision = 10, scale = 2)
  val treatmentMoney: BigDecimal? = null,
  /** 赔偿损失（元） */
  @Column(precision = 10, scale = 2)
  val compensateMoney: BigDecimal? = null,
  /** 跟进形式 */
  @Column(length = 50)
  val followType: String? = null,
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