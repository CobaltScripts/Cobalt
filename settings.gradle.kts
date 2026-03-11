pluginManagement {
  repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "Cobalt"

include("addons:bestiary-tracker")
