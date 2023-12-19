import utils.SaveUnpacker
import java.io.File

fun main(args: Array<String>) {
    // ONLY FOR TESTING
    val path = readln()
    val file = File(path)
    if (!file.exists()) {
        println("File does not exist!")
        return
    }
    SaveUnpacker.unpackSave(file)
}
