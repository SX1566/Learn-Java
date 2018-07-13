package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.SecondaryCategoryDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import tech.simter.category.po.Category.Status
import tech.simter.category.service.CategoryService

/**
 * 事故分类 Service 实现。
 *
 * @author JF
 */
@Component
@Transactional
class AccidentCategoryServiceImpl @Autowired constructor(
  private val categoryService: CategoryService
) : AccidentCategoryService {
  override fun findSecondaryCategories(includeDisabledStatus: Boolean, vararg primaryCategorySNs: String): Flux<SecondaryCategoryDto> {
    val childStatuses = if (includeDisabledStatus) arrayOf(Status.Enabled, Status.Disabled) else arrayOf(Status.Enabled)
    return categoryService
      .findChild(2, primaryCategorySNs.toList().toTypedArray(), childStatuses)
      .map { SecondaryCategoryDto(it.pid!!.sn, it.sn, it.name, it.status) }
  }
}