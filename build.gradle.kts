import dev.detekt.gradle.extensions.FailOnSeverity
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

detekt {
  buildUponDefaultConfig = true
  config.setFrom(rootProject.file("config/detekt/detekt.yml"))
  allRules = false
  ignoredBuildTypes = listOf()
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

val jij = configurations.create("jij")

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

tasks.named("check").configure {
  this.setDependsOn(this.dependsOn.filterNot {
    it is TaskProvider<*> && it.name == "detekt"
  })
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
