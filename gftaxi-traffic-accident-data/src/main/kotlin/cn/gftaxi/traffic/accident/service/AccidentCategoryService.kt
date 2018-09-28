package cn.gftaxi.traffic.accident.service

import cn.gftaxi.traffic.accident.dto.SecondaryCategoryDto
import reactor.core.publisher.Flux

/**
 * 事故分类 Service。
 *
 * @author JF
 */
interface AccidentCategoryService {
  /**
   * 获取指定一级分类下的二级分类列表。
   *
   * 返回结果按照一级分类 sn 正序 + 二级分类 stage 正序 + 二级分类 sn 正序排序。
   *
   * @param[includeDisabledStatus] false 仅返回 `Enabled` 状态， true 包含 `Enabled` 和 `Disabled` 状态
   * @param[primaryCategorySNs] 一级分类编码列表
   * @return 二级分类信息的 [Flux] 信号，无则返回 [Flux.empty]
   */
  fun findSecondaryCategories(includeDisabledStatus: Boolean, vararg primaryCategorySNs: String): Flux<SecondaryCategoryDto>
}