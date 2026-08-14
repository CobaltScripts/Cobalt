plugins {
  `kotlin-dsl`
}

group = "org.cobalt"

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain(25)
}
