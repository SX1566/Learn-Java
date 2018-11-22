package cn.gftaxi.traffic.accident.po.view

import cn.gftaxi.traffic.accident.po.view.converter.CarStatusConverter
import java.time.LocalDate
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * 车辆 PO。
 *
 * @author JF
 */
@Entity
@Table(name = "bs_car")
data class Car constructor(
  @Id val id: Int,
  /** 状态 */
  @Convert(converter = CarStatusConverter::class) val status: Status,
  /** 车牌归属，如"粤A" */
  val plateType: String,
  /** 车牌号码，如"C4X74" */
  val plateNo: String,
  /** 厂牌类型，如"现代" */
  val factoryType: String,
  /** 投产日期 */
  val operateDate: LocalDate,
  /** 所属公司 */
  val company: String,
  /** 所属车队 */
  val motorcadeName: String,
  /** 责任人信息 */
  val charger: String,
  /** 司机信息 */
  val driver: String,
  /** 自编号 */
  val code: String,
  /** 管理号 */
  val manageNo: Int
) {
  /**
   * 状态。
   */
  enum class Status(private val value: Short) {
    /**
     * 新购。
     */
    NewBuy(-2),
    /**
     * 草稿。
     */
    Draft(-1),
    /**
     * 在案。
     */
    Enabled(0),
    /**
     * 注销。
     */
    Disabled(1);

    fun value(): Short {
      return value
    }
  }
}