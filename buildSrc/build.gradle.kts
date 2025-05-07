plugins {
  base
  java
  `kotlin-dsl`
}

repositories {
  google()
  mavenCentral()
  maven(url = "https://plugins.gradle.org/m2")
  mavenLocal()
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(libs.android.build.tools.plugin)
  compileOnly(libs.android.build.tools.ddms)
  compileOnly(libs.kotlin.gradle.plugin)
}