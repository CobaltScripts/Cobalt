package org.cobalt.internal.helper

import com.google.gson.*
import java.io.File
import org.cobalt.Cobalt
import org.cobalt.api.module.ModuleManager

internal object Config {

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val modulesFile = File(Cobalt.mc.runDirectory, "config/cobalt/modules.json")

  fun loadModulesConfig() {
    if (!modulesFile.exists()) {
      modulesFile.parentFile.mkdirs()
      modulesFile.createNewFile()
    }

    val text = modulesFile.bufferedReader().use { it.readText() }
    if (text.isEmpty()) return

    val jsonArray = JsonParser.parseString(text).asJsonArray

    for (element in jsonArray) {
      val moduleObj = element.asJsonObject
      val moduleName = moduleObj.get("name").asString

      val module = ModuleManager.getModules().find {
        it.name == moduleName
      } ?: continue

      val settingsObj = moduleObj.getAsJsonObject("settings")
      if (settingsObj != null) {
        for ((key, value) in settingsObj.entrySet()) {
          val setting = module.getSettings().find {
            it.name == key
          } ?: continue

          setting.read(value)
        }
      }
    }
  }

  fun saveModulesConfig() {
    val jsonArray = JsonArray()

    for (module in ModuleManager.getModules()) {
      val moduleObj = JsonObject()

      moduleObj.add("name", JsonPrimitive(module.name))
      moduleObj.add("settings", JsonObject().apply {
        module.getSettings().forEach {
          add(it.name, it.write())
        }
      })

      jsonArray.add(moduleObj)
    }

    modulesFile.bufferedWriter().use {
      it.write(gson.toJson(jsonArray))
    }
  }

}
