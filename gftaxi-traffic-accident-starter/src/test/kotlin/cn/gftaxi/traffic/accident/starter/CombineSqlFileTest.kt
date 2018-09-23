package cn.gftaxi.traffic.accident.starter

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.FileOutputStream

/**
 * 合并所有依赖模块的 schema-create.sql、schema-drop.sql、data.sql 文件。
 *
 * @author RJ
 */
class CombineSqlFileTest {
  private val logger = LoggerFactory.getLogger(CombineSqlFileTest::class.java)

  @Test
  fun go() {
    val dbPlatform = "postgres"
    val paths = listOf(
      "tech/simter/kv/sql/$dbPlatform",
      "tech/simter/category/sql/$dbPlatform",
      "cn/gftaxi/traffic/accident/sql/$dbPlatform"
    )
    val classLoader = javaClass.classLoader
    combine2File("schema-drop.sql", paths, classLoader)
    combine2File("schema-create.sql", paths, classLoader)
    combine2File("data.sql", paths, classLoader)
  }

  private fun combine2File(name: String, paths: List<String>, classLoader: ClassLoader) {
    val toFile = FileOutputStream("target/$name")
    val up = paths.size - 1
    paths.forEachIndexed { index, path ->
      val resourceAsStream = classLoader.getResourceAsStream("$path/$name")
      if (resourceAsStream == null) {
        logger.warn("Combine $name: ignore not exists resource '$path/$name'")
      } else {
        logger.warn("Combine $name: from '$path/$name'")
        if (index in 1..up) toFile.write("\r\n\r\n\r\n".toByteArray())
        toFile.write("-- combine from $path/$name\r\n".toByteArray())
        toFile.write(resourceAsStream.readBytes())
      }
    }
    toFile.flush()
    toFile.close()
  }
}