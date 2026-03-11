import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.loom)
}

version = "1.0.0"
group = "org.cobalt.addon"

base {
  archivesName.set("bestiary-tracker")
}

dependencies {
  minecraft(libs.minecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.bundles.fabric)

  // Cobalt API - compile only, provided at runtime by the main mod
  compileOnly(project(":"))
}

tasks {
  compileKotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
  }

  // Package as a plain JAR (not a remapped mod JAR) for addon loading
  jar {
    from("src/main/resources")
    archiveClassifier.set("")
  }

  remapJar {
    // Disable remapping - addons are loaded by Cobalt's classloader, not Fabric
    isEnabled = false
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}
