package cn.gftaxi.traffic.accident.common

/**
 * 案件角色常数。
 *
 * @author RJ
 */
object AccidentRole {
  //== 事故报案角色 ==
  /** 事故报案查询角色 */
  const val ROLE_DRAFT_READ = "ACCIDENT_DRAFT_READ"
  /** 事故报案上报角色 */
  const val ROLE_DRAFT_SUBMIT = "ACCIDENT_DRAFT_SUBMIT"
  /** 事故报案修改角色 */
  const val ROLE_DRAFT_MODIFY = "ACCIDENT_DRAFT_MODIFY"
  /** 事故报案审核角色 */
  const val ROLE_DRAFT_CHECK = "ACCIDENT_DRAFT_CHECK"
  /** 事故报案查阅相关角色 */
  val ROLES_DRAFT_READ = arrayOf(ROLE_DRAFT_READ, ROLE_DRAFT_SUBMIT, ROLE_DRAFT_MODIFY, ROLE_DRAFT_CHECK)

  //== 事故登记角色 ==
  /** 事故登记查询角色 */
  const val ROLE_REGISTER_READ = "ACCIDENT_REGISTER_READ"
  /** 事故登记提交角色 */
  const val ROLE_REGISTER_SUBMIT = "ACCIDENT_REGISTER_SUBMIT"
  /** 事故登记修改角色 */
  const val ROLE_REGISTER_MODIFY = "ACCIDENT_REGISTER_MODIFY"
  /** 事故登记审核角色 */
  const val ROLE_REGISTER_CHECK = "ACCIDENT_REGISTER_CHECK"
  /** 事故登记查阅相关角色 */
  val ROLES_REGISTER_READ = arrayOf(ROLE_REGISTER_READ, ROLE_REGISTER_SUBMIT, ROLE_REGISTER_MODIFY, ROLE_REGISTER_CHECK)
}