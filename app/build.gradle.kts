import com.android.ddmlib.AndroidDebugBridge
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.protobuf)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
}

protobuf {
  protoc {
    val protocDep: MinimalExternalModuleDependency = libs.protobuf.protoc.get()
    artifact = "${protocDep.group}:${protocDep.name}:${protocDep.version}"
  }

  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        id("kotlin")
        id("java") {

        }
      }
    }
  }
}


android {
  namespace = "org.tfv.deskflow"
  buildToolsVersion = "36.0.0"
  compileSdk = 36

  defaultConfig {
    applicationId = "org.tfv.deskflow"
    minSdk = 33
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables.useSupportLibrary = true
  }
  buildFeatures {
    buildConfig = true
    compose = true
    viewBinding = true
    aidl = true
  }

  buildTypes {
    debug {
      isDebuggable = true
      isMinifyEnabled = false
      buildConfigField("boolean", "DEBUG", "true")
    }
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
      buildConfigField("boolean", "DEBUG", "false")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
  dependenciesInfo {
    includeInApk = true
    includeInBundle = true
  }

  sourceSets { getByName("main") { proto { srcDir("src/main/proto") } } }

  lint { disable.add("NullSafeMutableLiveData") }
}

dependencies {
  implementation(project(":client")) { exclude(group = "io.github.oshai") }

  implementation(libs.arrow.core)
  implementation(libs.arrow.fx.coroutines)

  implementation(project(":fontawesomepro-typeface-library"))

  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlin.logging.android)
  implementation(libs.slf4j.api)
  implementation(libs.protobuf.java.runtime)
  implementation(libs.protobuf.java.util)

  implementation(libs.protobuf.kotlin)

  ksp(libs.hilt.compiler)
  kspTest(libs.hilt.compiler)
  implementation(libs.hilt.core)
  implementation(libs.hilt.android)

  implementation(libs.google.accompanist.permissions)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.normal)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.android)
  implementation(libs.androidx.lifecycle.runtime.normal)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)
  implementation(libs.androidx.lifecycle.livedata.normal)
  implementation(libs.androidx.lifecycle.livedata.core.normal)
  implementation(libs.androidx.lifecycle.livedata.core.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.normal)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.lifecycle.process)
  implementation(libs.androidx.lifecycle.service)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.material3)

  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.uiautomator)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("installDebugEnableAccessibilityService") {
  dependsOn("installDebug")
  val ops = project.objects.newInstance<InjectedOps>()
  val execOps: ExecOperations = ops.execOps

  doLast {
    val adbPath = "${android.sdkDirectory}/platform-tools/adb"

    runCatching {
      AndroidDebugBridge.terminate()
      AndroidDebugBridge.init(true)
    }
    val adb =
      AndroidDebugBridge.createBridge(
        adbPath,
        true,
        1000L,
        TimeUnit.MILLISECONDS,
      )
    adb.startAdb(1000L, TimeUnit.MILLISECONDS)
    println("ADB server started: ${adb.isConnected}")

    require(adb.devices.isNotEmpty()) {
      "At least one device must be connected to install app & enable the AccessibilityService."
    }

    adb.devices.forEach { device ->
      println(
        "Activating AccessibilityService on Device ${device.serialNumber}"
      )
      if (!device.isOnline) {
        println("Device ${device.serialNumber} is not online. Skipping...")
        return@forEach
      }

      execOps.exec {
        commandLine(
          adbPath,
          "-s",
          device.serialNumber,
          "shell",
          "settings",
          "put",
          "secure",
          "enabled_accessibility_services",
          "\"org.tfv.deskflow/org.tfv.deskflow.services.GlobalInputService\"",
        )
      }
      execOps.exec {
        commandLine(
          adbPath,
          "-s",
          device.serialNumber,
          "shell",
          "am",
          "start",
          "-n",
          "org.tfv.deskflow/org.tfv.deskflow.ui.activities.RootActivity",
        )
      }
    }

    runCatching { AndroidDebugBridge.terminate() }
  }
}
