package cn.gftaxi.traffic.accident.po.view

import java.time.LocalDate
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
  /** 状态：-2:新购，-1:草稿，0-在案，1-注销 */
  val status: Int,
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
  val motorcade: String,
  /** 责任人信息 */
  val charger: String,
  /** 司机信息 */
  val driver: String,
  /** 自编号 */
  val code: String,
  /** 管理号 */
  val manageNo: Int
)