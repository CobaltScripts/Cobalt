import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.loom)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.detekt)
  `maven-publish`
}

version = providers.gradleProperty("modVersion").get()
group = providers.gradleProperty("baseGroup").get()

base {
  archivesName = providers.gradleProperty("modName").get()
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}

repositories {
  mavenCentral()
  maven("https://maven.ccbluex.net/snapshots")
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

loom {
  accessWidenerPath = rootProject.file("src/main/resources/cobalt.accesswidener")
}

val jij: Configuration by configurations.creating

jij.excludeProvidedLibs()

dependencies {
  minecraft(libs.minecraft)

  api(libs.fabric.loader)
  api(libs.fabric.api)
  api(libs.fabric.kotlin)

  jij(libs.skija.shared)
  jij(libs.discordIpc)

  runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
}

addResolvedDependencies(jij, "compileOnly", "include", "api")

tasks {
  processResources {
    val fabricLoaderVersion = libs.versions.fabric.loader.get()
    val minecraftVersion = libs.versions.minecraft.version.get()

    inputs.property("version", project.version)
    inputs.property("fabricLoaderVersion", fabricLoaderVersion)
    inputs.property("minecraftVersion", minecraftVersion)

    filesMatching("fabric.mod.json") {
      expand(
        "version" to project.version,
        "fabricLoaderVersion" to fabricLoaderVersion,
        "minecraftVersion" to minecraftVersion,
      )
    }
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 25
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_25
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

detekt {
  buildUponDefaultConfig = true
  config.setFrom(rootProject.file("config/detekt/detekt.yml"))
  allRules = false
  ignoredBuildTypes = listOf()
}

fun Project.addResolvedDependencies(
  from: Configuration,
  vararg toConfigurations: String,
) {
  val resolvedDeps = from.incoming.resolutionResult.allDependencies
    .map { dep ->
      val requested = dep.requested.displayName
      dependencies.create(requested) {
        (this as? ModuleDependency)?.isTransitive = false
      }
    }

  toConfigurations.forEach { configName ->
    configurations.named(configName).configure {
      withDependencies {
        addAll(resolvedDeps)
      }
    }
  }
}

fun Configuration.excludeProvidedLibs() = apply {
  exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
  exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
  exclude(group = "org.jetbrains.kotlinx", module = "atomicfu")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-datetime")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-io-core")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-io-bytestring")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-cbor")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-core")
  exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-serialization-json")

  exclude(group = "it.unimi.dsi", module = "fastutil")
  exclude(group = "com.google.guava", module = "guava")
  exclude(group = "com.google.code.gson", module = "gson")
  exclude(group = "net.java.dev.jna", module = "jna")
  exclude(group = "commons-codec", module = "commons-codec")
  exclude(group = "commons-io", module = "commons-io")
  exclude(group = "org.apache.commons", module = "commons-compress")
  exclude(group = "org.apache.commons", module = "commons-lang3")
  exclude(group = "org.apache.logging.log4j", module = "log4j-core")
  exclude(group = "org.apache.logging.log4j", module = "log4j-api")
  exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
  exclude(group = "org.slf4j", module = "slf4j-api")
  exclude(group = "com.mojang", module = "authlib")
  exclude(group = "org.lwjgl", module = "lwjgl")

  exclude(group = "io.netty", module = "netty-buffer")
  exclude(group = "io.netty", module = "netty-codec")
  exclude(group = "io.netty", module = "netty-codec-base")
  exclude(group = "io.netty", module = "netty-codec-compression")
  exclude(group = "io.netty", module = "netty-codec-http")
  exclude(group = "io.netty", module = "netty-common")
  exclude(group = "io.netty", module = "netty-handler")
  exclude(group = "io.netty", module = "netty-resolver")
  exclude(group = "io.netty", module = "netty-transport")
  exclude(group = "io.netty", module = "netty-transport-classes-epoll")
  exclude(group = "io.netty", module = "netty-transport-classes-kqueue")
  exclude(group = "io.netty", module = "netty-transport-native-epoll")
  exclude(group = "io.netty", module = "netty-transport-native-kqueue")
  exclude(group = "io.netty", module = "netty-transport-native-unix-common")
}
