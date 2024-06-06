val d2vVersion: String by project
val chartsVersion: String by project
val tornadoFxVersion: String by project
val koinVersion: String by project
val coroutinesVersion: String by project
val ktorVersion: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

group = "ru.jspace"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/data2viz/p/maven/dev")
    maven("https://maven.pkg.jetbrains.space/data2viz/p/maven/public")
}

javafx {
    version = "22.0.1"
    modules = listOf("javafx.controls")
}

dependencies {
    implementation("no.tornado:tornadofx:$tornadoFxVersion")
    implementation("io.data2viz.d2v:core-jvm:$d2vVersion")
    implementation("io.data2viz.charts:core:$chartsVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$coroutinesVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-sse-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

kotlin {
    jvmToolchain(21)
}