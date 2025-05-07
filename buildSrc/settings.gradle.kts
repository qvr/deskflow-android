
pluginManagement {

  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2") }
    maven { url = uri("https://maven.pkg.github.com/3fv/kdux") }
  }

}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      // point at the same TOML you use in the root build
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
