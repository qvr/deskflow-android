
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose)
}


android {
  namespace = "org.tfv.deskflow.android.iconics"
  buildToolsVersion = "36.0.0"
  compileSdk = 36

  defaultConfig {
    minSdk = 33
    version = "6.0.0.0"
    consumerProguardFiles.clear()
    consumerProguardFiles.add(File(projectDir, "consumer-proguard-rules.pro"))
    resValue("string", "fontawesomepro_version", "6.0.0.0")
  }
  buildFeatures {
    compose = true
  }

  buildTypes {
    debug {
      isMinifyEnabled = false
    }
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
//  dependenciesInfo {
//    includeInApk = true
//    includeInBundle = true
//  }


  lint {
    disable.add("NullSafeMutableLiveData")
  }

}

dependencies {

  implementation(libs.iconics.typeface.api)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)

  implementation(compose.runtime)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.material3)
  testImplementation(libs.junit)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

}
