import exceptions.UnpackedSaveNotFoundException
import org.slf4j.LoggerFactory
import utils.SaveUtils
import utils.VersionUtils
import java.io.File
import kotlin.system.exitProcess

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

    val saveFile: File
    val directory: String

    try {
        print("Enter the path to your F1 2022/2023 save file: ")
        val path = readln()
        saveFile = File(path)
        directory = saveFile.parentFile.absolutePath
        SaveUtils.SaveUnpacker.unpackSave(saveFile)
    } catch (unpackedSaveNotFoundException: UnpackedSaveNotFoundException) {
        val message = "UnpackedSaveNotFoundException: ${unpackedSaveNotFoundException.message}"
        logger.error(message)
        exitProcess(1)
    } catch (exception: Exception) {
        logger.error("An unexpected error occurred.", exception)
        exitProcess(1)
    }

    SaveUtils.SaveRepacker.repackSave(
        directory,
        "${saveFile.name.substring(0 until saveFile.name.lastIndexOf('.'))}.repacked.sav"
    )

    println("Thank you for using F1Manager-Save-Editor! Have a nice day!")
}
