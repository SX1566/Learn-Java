package cn.gftaxi.traffic.accident.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import tech.simter.category.po.Category
import tech.simter.category.service.CategoryService
import kotlin.test.assertEquals

/**
 * 测试事故分类 Service 实现。
 *
 * @author JF
 */
@SpringJUnitConfig(AccidentCategoryServiceImpl::class)
@MockBean(CategoryService::class)
class AccidentCategoryServiceImplTest @Autowired constructor(
  private val accidentCategoryService: AccidentCategoryService,
  private val categoryService: CategoryService
) {
  @Test
  fun findSecondaryCategories() {
    // mock
    val parentParentId = 2
    val parentSNs = arrayOf("Sn")
    val enabled = arrayOf(Category.Status.Enabled)
    val enabledAndDisabled = arrayOf(Category.Status.Enabled, Category.Status.Disabled)
    val category0 = Category(null, null, Category.Status.Enabled, "category0", "2")
    val category1 = Category(null, category0, Category.Status.Enabled, "category1", "2")
    val category2 = Category(null, category1, Category.Status.Disabled, "category2", "1")
    val category3 = Category(null, category2, Category.Status.Enabled, "category3", "2")
    `when`(categoryService.findChild(parentParentId, parentSNs, enabled))
      .thenReturn(Flux.just(category1, category3))
    `when`(categoryService.findChild(parentParentId, parentSNs, enabledAndDisabled))
      .thenReturn(Flux.just(category1, category2, category3))

    // invoke
    val actualNoIncludeDisabled = accidentCategoryService.findSecondaryCategories(false, *parentSNs)
    val actualIncludeDisabled = accidentCategoryService.findSecondaryCategories(true, *parentSNs)

    // verify
    StepVerifier.create(actualNoIncludeDisabled)
      .consumeNextWith {
        assertEquals(category1.pid!!.sn, it.belong)
        assertEquals(category1.sn, it.sn)
        assertEquals(category1.name, it.name)
        assertEquals(category1.status, it.status)
      }
      .consumeNextWith {
        assertEquals(category3.pid!!.sn, it.belong)
        assertEquals(category3.sn, it.sn)
        assertEquals(category3.name, it.name)
        assertEquals(category3.status, it.status)
      }
      .verifyComplete()
    StepVerifier.create(actualIncludeDisabled)
      .consumeNextWith {
        assertEquals(category1.pid!!.sn, it.belong)
        assertEquals(category1.sn, it.sn)
        assertEquals(category1.name, it.name)
        assertEquals(category1.status, it.status)
      }
      .consumeNextWith {
        assertEquals(category2.pid!!.sn, it.belong)
        assertEquals(category2.sn, it.sn)
        assertEquals(category2.name, it.name)
        assertEquals(category2.status, it.status)
      }
      .consumeNextWith {
        assertEquals(category3.pid!!.sn, it.belong)
        assertEquals(category3.sn, it.sn)
        assertEquals(category3.name, it.name)
        assertEquals(category3.status, it.status)
      }
      .verifyComplete()
    verify(categoryService).findChild(parentParentId, parentSNs, enabled)
    verify(categoryService).findChild(parentParentId, parentSNs, enabledAndDisabled)
  }
}