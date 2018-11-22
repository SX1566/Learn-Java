package cn.gftaxi.traffic.accident.po.view

import cn.gftaxi.traffic.accident.po.view.converter.CarManStatusConverter
import java.time.LocalDate
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * 司机责任人 PO。
 *
 * @author JF
 */
@Entity
@Table(name = "bs_carman")
data class CarMan constructor(
  @Id val id: Int,
  /** UID */
  val uid: String,
  /** 状态 */
  @Convert(converter = CarManStatusConverter::class) val status: Status,
  /** 类别 */
  val type: Type,
  /** 姓名 */
  val name: String,
  /** 性别 */
  val sex: Sex,
  /** 籍贯 */
  val origin: String,
  /** 身份证号 */
  val idCardNo: String,
  /** 初次领证日期 */
  val initialLicenseDate: LocalDate,
  /** 准驾车型 */
  val model: String,
  /** 入职日期 */
  val workDate: LocalDate,
  /** 服务资格证号 */
  val serviceCertNo: String,
  /** 电话 */
  val phone: String
) {
  /**
   * 状态。
   */
  enum class Status(private val value: Short) {
    /**
     * 草稿。
     */
    Draft(-1),
    /**
     * 启用中。
     */
    Enabled(0),
    /**
     * 已禁用。
     */
    Disabled(1),
    /**
     * 已删除。
     */
    Deleted(2);

    fun value(): Short {
      return value
    }
  }

  /**
   * 类别。
   */
  enum class Type(private val value: Short) {
    /**
     * 司机。
     */
    Driver(0),
    /**
     * 责任人。
     */
    Charger(1),
    /**
     * 司机和责任人。
     */
    All(2);

    fun value(): Short {
      return value
    }
  }

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