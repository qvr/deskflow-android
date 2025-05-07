import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version libs.versions.kotlin
    `java-library`
}

kotlin {
    jvmToolchain(17)

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.core.jvm)
    implementation(libs.kotlin.logging.jvm)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform() // Required for JUnit 5
    testLogging {
        events(*TestLogEvent.values())
    }
}