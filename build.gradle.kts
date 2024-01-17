import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneId
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

val writeProjectProperties by tasks.registering(WriteProperties::class) {
    destinationFile = file("${projectDir}/src/main/resources/version.properties")
    encoding = "UTF-8"
    property("version", versionProperty)
    property("stage", stageProperty)
    property("revision", revisionProperty)
    property(
        "buildDate",
        ZonedDateTime
            .now(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ofPattern("E, MMM dd yyyy"))
    )
}

val shadowJarVersion: String
    get() {
        var result = versionProperty
        if (stageProperty == "SNAPSHOT" || stageProperty == "alpha" || stageProperty == "beta" || stageProperty == "rc") {
            result += "-$stageProperty"
        }
        result += "+$revisionProperty"
        return result
    }

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

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(writeProjectProperties)
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    destinationDirectory.set(file("${projectDir}/build/distributions"))
    archiveVersion.set(shadowJarVersion)
    archiveClassifier.set("")
}

kotlin {
//    jvmToolchain(21)
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
