val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "com.testHelios"
version = "0.0.1"
application {
    mainClass.set("com.testHelios.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.github.papsign:Ktor-OpenAPI-Generator:-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}