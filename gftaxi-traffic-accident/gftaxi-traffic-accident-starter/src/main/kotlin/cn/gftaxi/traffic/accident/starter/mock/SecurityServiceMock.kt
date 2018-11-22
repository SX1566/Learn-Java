package cn.gftaxi.traffic.accident.starter.mock

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import tech.simter.security.SecurityService

/**
 * 放行所有权限的 mock。
 */
@Service
@Primary
class SecurityServiceMock : SecurityService {
  override fun hasAllRole(vararg roles: String?): Boolean {
    return true
  }

  override fun hasRole(role: String?): Boolean {
    return true
  }

  override fun hasAnyRole(vararg roles: String?): Boolean {
    return true
  }

  override fun verifyHasAllRole(vararg roles: String?) {
    // do nothing
  }

  override fun verifyHasAnyRole(vararg roles: String?) {
    // do nothing
  }

  override fun verifyHasRole(role: String?) {
    // do nothing
  }
}