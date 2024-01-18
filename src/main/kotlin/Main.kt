import exceptions.UnpackedSaveNotFoundException
import org.slf4j.LoggerFactory
import utils.SaveUnpacker
import utils.SaveRepacker
import utils.VersionUtils
import java.io.File

private val logger = LoggerFactory.getLogger("F1Manager-Save-Editor")

fun main(args: Array<String>) {
    logger.debug("Current directory: ${File("").absolutePath}")
    logger.debug("Clearing runtime files from previous runs...")
    File("unpacked_save").deleteRecursively()
    logger.debug("Runtime files cleared.")
    logger.debug("Starting F1Manager-Save-Editor...")
    VersionUtils.loadVersionProperties()

    println("Welcome to F1Manager-Save-Editor!")
    println("version: ${VersionUtils.version}; built ${VersionUtils.buildDateProperty}")
    if (VersionUtils.stageProperty != "stable") {
        logger.warn("Running an ${VersionUtils.stageProperty} build of F1Manager-Save-Editor.")
        println(
            if (VersionUtils.stageProperty.startsWith("rc")) {
                "THIS IS A RELEASE CANDIDATE BUILD OF F1MANAGER-SAVE-EDITOR. USE AT YOUR OWN RISK."
            } else {
                "THIS IS AN EXPERIMENTAL BUILD OF F1MANAGER-SAVE-EDITOR. USE AT YOUR OWN RISK."
            }
        )
    }

    try {
        // ONLY FOR TESTING
        val path = readln()
        val file = File(path)
        val directory = file.parentFile.absolutePath
        SaveUnpacker.unpackSave(file)
        SaveRepacker.repackSave(
            directory,
            "${file.name.substring(0 until file.name.lastIndexOf('.'))}.repacked.sav"
        )
    } catch (unpackedSaveNotFoundException: UnpackedSaveNotFoundException) {
        val message = "UnpackedSaveNotFoundException: ${unpackedSaveNotFoundException.message}"
        logger.error(message)
    } catch (exception: Exception) {
        logger.error("An unexpected error occurred.", exception)
    }
}
