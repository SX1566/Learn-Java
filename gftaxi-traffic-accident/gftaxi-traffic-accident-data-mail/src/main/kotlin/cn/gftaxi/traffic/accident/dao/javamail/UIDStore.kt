package cn.gftaxi.traffic.accident.dao.javamail

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class UIDStore(
  private val file: File
) {
  private val ids = HashMap<String, Boolean>()
  private val logger: Logger = LoggerFactory.getLogger(UIDStore::class.java)

  init {
    if (!file.parentFile.exists()) {
      file.parentFile.mkdirs()
      logger.warn("创建邮件ID缓存文件所在的目录：${file.parent}")
    }
    this.load()
  }

  private fun load() {
    if (file.exists()) {
      val reader = BufferedReader(FileReader(file))
      reader.use {
        var id: String? = it.readLine()
        while (id != null && !id.isNullOrEmpty()) {
          ids[id] = false
          id = it.readLine()
        }
      }
    }
  }

  @Synchronized
  fun store() {
    val newIds = ids.filter { it.value }
    if (newIds.isEmpty()) return

    val writer = FileWriter(file, true)
    writer.use { w ->
      newIds.forEach {
        logger.debug("--------new mail id=$it")
        w.write(it.key)
        w.write("\r\n")
        ids[it.key] = false
      }
    }
  }

  fun contains(id: String): Boolean {
    return ids.containsKey(id)
  }

  @Synchronized
  fun add(id: String) {
    ids[id] = true
  }

  val size: Int get() = ids.size
}