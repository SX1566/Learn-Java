package cn.gftaxi.traffic.accident.po.view

import java.time.LocalDate
import javax.persistence.Column
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
  /** 状态：-1:草稿，0-启用中，1-已禁用，2-已删除 */
  val status: Int,
  /** 类别：0-司机，1-责任人，2-司机和责任人 */
  val type: Int,
  /** 姓名 */
  val name: String,
  /** 性别：0-未设置，1-男，2-女 */
  val sex: Int,
  /** 籍贯 */
  val origin: String,
  /** 身份证号 */
  @Column(name = "cert_identity") val identityCard: String,
  /** 初次领证日期 */
  @Column(name = "cert_driving_first_date") val firstTimeOfDriverLicense: LocalDate,
  /** 准驾车型 */
  val model: String,
  /** 入职日期 */
  val workDate: LocalDate,
  /** 服务资格证号 */
  @Column(name = "cert_fwzg") val serviceAccount: String,
  /** 电话 */
  val phone: String
)