package org.cobalt.ui.theme

import com.google.gson.Gson
import java.awt.Color
import java.nio.file.Files
import kotlin.io.path.notExists
import org.cobalt.Cobalt.configDir
import org.cobalt.util.config.SimpleConfig

object ThemeManager {

  private val gson = Gson()
  private val defaultThemes = listOf(
    "/assets/cobalt/themes/steelBlue.json",
    "/assets/cobalt/themes/forestEmerald.json",
    "/assets/cobalt/themes/midnightViolet.json",
    "/assets/cobalt/themes/sunsetHorizon.json",
    "/assets/cobalt/themes/crimsonNoir.json",
    "/assets/cobalt/themes/arcticFrost.json",
  )

  private val settingsConfig = SimpleConfig(
    configDir.resolve("settings.json").toString(),
    Settings::class.java
  )

  @JvmStatic
  lateinit var activeTheme: Theme
    private set

  @JvmStatic
  var themes: Map<String, Theme> = emptyMap()
    private set

  @JvmStatic
  internal fun loadThemes() {
    reloadThemes()

    val savedSettings = settingsConfig.load() ?: Settings("Steel Blue")
    activeTheme = themes[savedSettings.activeThemeName] ?: themes.values.first()
  }

  @JvmStatic
  internal fun reloadThemes() {
    themes = emptyMap()

    val folder = configDir
      .resolve("themes")
      .apply(Files::createDirectories)

    val hasThemes = folder.toFile()
      .walkTopDown()
      .any { it.isFile && it.extension == "json" }

    if (!hasThemes) {
      defaultThemes.forEach { resourcePath ->
        val resourceName = resourcePath.substringAfterLast('/')
        val target = folder.resolve(resourceName)

        if (target.notExists()) {
          javaClass.getResourceAsStream(resourcePath)
            ?.use { input ->
              Files.copy(input, target)
            }
            ?: error("Theme resource not found: $resourcePath")
        }
      }
    }

    themes = folder.toFile()
      .walkTopDown()
      .filter { it.isFile && it.extension == "json" }
      .mapNotNull { file ->
        runCatching {
          val raw = gson.fromJson(file.readText(), ThemeJson::class.java)
          raw.name to raw.toTheme()
        }.getOrNull()
      }
      .toMap()
  }

  @JvmStatic
  fun changeTheme(theme: Theme) {
    activeTheme = theme
    settingsConfig.save(Settings(theme.name))
  }

  private data class Settings(
    val activeThemeName: String,
  )

  private data class ColorJson(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 255,
  ) {

    fun toColor() =
      Color(red, green, blue, alpha)

  }

  private data class ThemeJson(
    val name: String,
    val backgroundPrimary: ColorJson,
    val backgroundSecondary: ColorJson,
    val border: ColorJson,
    val accentPrimary: ColorJson,
    val accentSecondary: ColorJson,
    val textPrimary: ColorJson,
    val textSecondary: ColorJson,
    val textMuted: ColorJson,
    val textDisabled: ColorJson,
    val success: ColorJson,
    val warning: ColorJson,
    val error: ColorJson,
    val info: ColorJson,
  ) {

    fun toTheme() = Theme(
      name = name,
      backgroundPrimary = backgroundPrimary.toColor(),
      backgroundSecondary = backgroundSecondary.toColor(),
      border = border.toColor(),
      accentPrimary = accentPrimary.toColor(),
      accentSecondary = accentSecondary.toColor(),
      textPrimary = textPrimary.toColor(),
      textSecondary = textSecondary.toColor(),
      textMuted = textMuted.toColor(),
      textDisabled = textDisabled.toColor(),
      success = success.toColor(),
      warning = warning.toColor(),
      error = error.toColor(),
      info = info.toColor()
    )

  }

}
