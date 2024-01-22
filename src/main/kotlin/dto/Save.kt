package dto

import utils.SaveUtils
import java.io.File

/*
 * Save.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Monday Jan. 22.
 */

/**
 * The save file.
 * @param path The path to the save file.
 */
class Save(path: String) {
    /**
     * The File object of the save file.
     */
    val file: File = File(path)

    /**
     * The directory string of the save file.
     */
    val directory: String = file.parentFile.absolutePath

    /**
     * The corresponding SaveUtils object.
     */
    private val util = SaveUtils(this)

    /**
     * Unpacks the save file.
     */
    fun unpack() {
        util.SaveUnpacker().unpackSave()
    }

    /**
     * Repacks the save file.
     */
    fun repack() {
        util.SaveRepacker().repackSave()
    }
}
