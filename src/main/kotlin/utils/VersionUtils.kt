package utils

import java.util.*

/*
 * VersionUtils.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Wednesday Jan. 17.
 */

class VersionUtils {
    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        private val properties = Properties()

        fun loadVersionProperties() {
            properties.load(VersionUtils::class.java.getResourceAsStream("/version.properties"))
        }

        val versionProperty: String
            get() = properties.getProperty("version")

        val stageProperty: String
            get() = properties.getProperty("stage")

        val formattedStageProperty: String
            get() {
                var formatted = stageProperty
                formatted = formatted.replace(regex = Regex("SNAPSHOT"), replacement = "SNAPSHOT")
                formatted = formatted.replace(regex = Regex("alpha\\."), replacement = "Alpha ")
                formatted = formatted.replace(regex = Regex("alpha"), replacement = "Alpha")
                formatted = formatted.replace(regex = Regex("beta\\."), replacement = "Beta ")
                formatted = formatted.replace(regex = Regex("beta"), replacement = "Beta")
                formatted = formatted.replace(regex = Regex("rc\\."), replacement = "Release Candidate ")
                formatted = formatted.replace(regex = Regex("rc"), replacement = "Release Candidate")
                return formatted
            }

        val revisionProperty: String
            get() = properties.getProperty("revision")

        val uppercaseRevisionProperty: String
            get() = revisionProperty.uppercase()

        val buildDateProperty: String
            get() = properties.getProperty("buildDate")

        val version: String
            get() {
                return if (stageProperty == "stable")  {
                    "$versionProperty ($uppercaseRevisionProperty)"
                } else {
                    "$versionProperty $formattedStageProperty ($uppercaseRevisionProperty)"
                }
            }
    }
}
