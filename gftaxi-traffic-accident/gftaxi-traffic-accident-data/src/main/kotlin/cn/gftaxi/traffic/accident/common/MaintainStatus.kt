package cn.gftaxi.traffic.accident.common

/***
 * 审查状态
 *
 * 用于标记维修过程处理的状态
 *
 * @author SX
 */


enum class MaintainStatus(private val value: Short) {

  /***
   * 待提交、草稿
   */
  ToSubmit(1),

  /***
   * 待审核
   */
  ToCheck(2),

  /***
   * 审核不通过
   */
  Rejected(8),

  /***
   * 审核通过
   */
  Approver(16),

  /***
   *  维修中
   */
  Repairing(32),

  /***
   *  维修完成
   */
  Closed(64);

  fun value(): Short {
    return value
  }

}