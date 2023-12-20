import utils.SaveUnpacker
import utils.SaveRepacker
import java.io.File

fun main(args: Array<String>) {
    // ONLY FOR TESTING
    val path = readln()
    val file = File(path)
    if (!file.exists()) {
        println("File does not exist!")
        return
    }
    val directory = file.parentFile.absolutePath
    SaveUnpacker.unpackSave(file)
    SaveRepacker.repackSave(directory, file.name)
}
