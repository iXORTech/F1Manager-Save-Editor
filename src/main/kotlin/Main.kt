import org.slf4j.LoggerFactory
import utils.SaveUnpacker
import utils.SaveRepacker
import java.io.File

private val logger = LoggerFactory.getLogger("F1Manager-Save-Editor")

fun main(args: Array<String>) {
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
}
