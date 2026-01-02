pluginManagement {

  repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    gradlePluginPortal()
  }

  val loom_version: String by settings
  val fabric_kotlin_version: String by settings

  plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("fabric-loom") version loom_version
    id("org.jetbrains.kotlin.jvm") version fabric_kotlin_version.split("+kotlin.")[1].split("+")[0]
  }

}

rootProject.name = "Cobalt"
