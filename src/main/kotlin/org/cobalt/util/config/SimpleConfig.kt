package org.cobalt.util.config

import com.google.gson.GsonBuilder
import java.io.File

class SimpleConfig<T>(path: String, private val clazz: Class<T>) {

  private val file = File(path)

  fun load(): T? {
    if (!file.exists()) {
      return null
    }

    return gson.fromJson(file.readText(), clazz)
  }

  fun save(data: T) {
    file.parentFile?.mkdirs()
    file.writeText(gson.toJson(data))
  }

  companion object {
    private val gson = GsonBuilder()
      .setPrettyPrinting()
      .create()
  }

}
