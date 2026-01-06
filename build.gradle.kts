import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.io.ByteArrayOutputStream

plugins {
  kotlin("jvm")
  id("fabric-loom")
  id("org.jetbrains.dokka") version "1.9.20"
  `maven-publish`
  java
}

val baseGroup: String by project
val lwjglVersion: String by project
val modVersion: String by project
val modName: String by project

version = modVersion
group = baseGroup

base {
  archivesName = modName
}

val docVersionsDir = projectDir.resolve("docs-versions")
val currentVersion = version.toString()
val currentVersionDir = docVersionsDir.resolve(currentVersion)

repositories {
  mavenCentral()
  maven("https://maven.meteordev.org/releases")
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
  maven("https://api.modrinth.com/maven")
}

dependencies {
  minecraft("com.mojang:minecraft:${property("minecraft_version")}")
  mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")

  modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
  modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
  modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

  modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

  modImplementation("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")
  include("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")

  listOf("windows", "linux", "macos", "macos-arm64").forEach {
    modImplementation("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:natives-$it")
    include("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:natives-$it")
  }

  implementation("meteordevelopment:discord-ipc:1.1")
  include("meteordevelopment:discord-ipc:1.1")

  dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.20")
  dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin:1.9.20")

  implementation("org.reflections:reflections:0.10.2")
  include("org.reflections:reflections:0.10.2")
}

tasks {
  processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
      expand(getProperties())
      expand(mutableMapOf("version" to project.version))
    }
  }

  publishing {
    publications {
      create<MavenPublication>("mavenJava") {
        artifact(remapJar) {
          builtBy(remapJar)
        }
        artifact(kotlinSourcesJar) {
          builtBy(remapSourcesJar)
        }
      }
    }
  }

  compileKotlin {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
    }
  }
}

tasks.dokkaHtml {
  outputDirectory.set(currentVersionDir)
  moduleName.set(project.name)
  moduleVersion.set(currentVersion)

  pluginsMapConfiguration.set(
    mapOf(
      "org.jetbrains.dokka.versioning.VersioningPlugin" to """
        {
          "version": "$currentVersion",
          "olderVersionsDir": "${docVersionsDir.absolutePath.replace("\\", "\\\\")}",
          "renderVersionsNavigationOnAllPages": true
        }
      """.trimIndent()
    )
  )

  suppressObviousFunctions.set(true)
  suppressInheritedMembers.set(true)

  dokkaSourceSets {
    named("main") {
      jdkVersion.set(21)

      perPackageOption {
        matchingRegex.set("org\\.cobalt\\.internal($|\\.).*")
        suppress.set(true)
      }

      sourceLink {
        localDirectory.set(file("src/main/kotlin"))
        remoteUrl.set(URI("https://github.com/CobaltScripts/Cobalt/blob/${getGitBranch()}/src/main/kotlin").toURL())
        remoteLineSuffix.set("#L")
      }

      externalDocumentationLink {
        url.set(URI("https://docs.oracle.com/javase/8/docs/api/").toURL())
      }
    }
  }
}

fun getGitBranch(): String {
  val out = ByteArrayOutputStream()

  providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
    standardOutput = out
  }

  return out.toString().trim()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}
