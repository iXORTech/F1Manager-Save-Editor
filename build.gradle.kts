plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "tech.ixor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Logback
    implementation("ch.qos.logback:logback-classic:_")

    // Testing Dependencies
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
//    jvmToolchain(21)
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}