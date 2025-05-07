import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version libs.versions.kotlin
    application
}

kotlin {
    jvmToolchain(17)

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("org.tfv.deskflow.client.cli.MainKt")
}

dependencies {
    implementation(project(":client"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.core.jvm)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform() // Required for JUnit 5
    testLogging {
        events(*TestLogEvent.values())
    }
}