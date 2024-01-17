import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.Properties

val projectProperties = "${projectDir}/project.properties"

fun runShellCommand(command: String, pwd: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = pwd
        commandLine = command.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

fun getProperties(file: String, key: String): String {
    val fileInputStream = FileInputStream(file)
    val props = Properties()
    props.load(fileInputStream)
    return props.getProperty(key)
}

val versionProperty: String
    get() = getProperties(projectProperties, "version")

val stageProperty: String
    get() = getProperties(projectProperties, "stage")

val revisionProperty: String
    get() = runShellCommand("git rev-parse --short=7 HEAD")

plugins {
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow")
    application
}

group = "tech.ixor"
version = "$versionProperty-$stageProperty+$revisionProperty"

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
