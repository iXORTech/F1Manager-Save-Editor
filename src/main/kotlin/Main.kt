import exceptions.UnpackedSaveNotFoundException
import org.slf4j.LoggerFactory
import utils.SaveUnpacker
import utils.SaveRepacker
import java.io.File

private val logger = LoggerFactory.getLogger("F1Manager-Save-Editor")

fun main(args: Array<String>) {
    try {
        logger.debug("Current directory: ${File("").absolutePath}")
        logger.debug("Clearing runtime files from previous runs...")
        File("unpacked_save").deleteRecursively()
        logger.debug("Runtime files cleared.")
        logger.debug("Starting F1Manager-Save-Editor...")

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
