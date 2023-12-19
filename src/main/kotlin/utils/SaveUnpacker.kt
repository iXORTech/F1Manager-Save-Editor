package utils

import java.io.File

/*
 * SaveUnpacker.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Tuesday Dec. 19.
 *
 * Modified by Qian Qian "Cubik" on Tuesday Dec. 19.
 * - Backup prompt and backup functionality.
 * - Unpack the chunk1 data (data that is not a part of the real database) from the save file.
 */

/**
 * Utility class to unpack the F1 Manager save file.
 */
class SaveUnpacker {
    companion object {
        val CHNUK1_NAME = "chunk1"
        val MAIN_DB_NAME = "main.db"
        val BACKUP_DB_NAME = "backup1.db"
        val BACKUP_DB2_NAME = "backup2.db"

        /**
         * Unpack the F1 Manager save file.
         * @param save The save file to unpack.
         */
        fun unpackSave(save: File) {
            // Get path of the save file.
            val path = save.absolutePath
            // Get the directory to save the unpacked files.
            val dir = File(path.substring(0, path.lastIndexOf('.')))

            // Backup the save file.
            println("Make sure you have a backup of your save file before proceeding!")
            print("Do you want the program to make a backup of your save file? (y/n)")
            val backup = readln().lowercase() == "y"
            print('\n')
            if (backup) {
                val backupFile = File(save.absolutePath + ".bak")
                if (backupFile.exists()) {
                    backupFile.delete()
                }
                save.copyTo(backupFile)
            }

            println("Unpacking save file...")

            // Read the save file.
            val saveData = save.readBytes()

            // Signature before the packed database.
            // \x00\x05\x00\x00\x00\x4E\x6F\x6E\x65\x00\x05\x00\x00\x00\x4E\x6F\x6E\x65\x00
            val preDatabaseSig = byteArrayOf(
                0x00, 0x05, 0x00, 0x00, 0x00, 0x4E, 0x6F, 0x6E, 0x65,
                0x00, 0x05, 0x00, 0x00, 0x00, 0x4E, 0x6F, 0x6E, 0x65, 0x00
            )

            // Get the offset of the packed database.
            val databaseOffset = saveData.indexOf(preDatabaseSig) + preDatabaseSig.size + 4

            // Save to part of the save that is not the database for repacking.
            val chunk1File = File(dir, CHNUK1_NAME)
            if (chunk1File.exists()) {
                chunk1File.delete()
            }
            val chunk1Data = saveData.sliceArray(0 until databaseOffset)
            chunk1File.writeBytes(chunk1Data)
        }
    }
}

/**
 * Get the index of a byte array in another byte array.
 * @param element The byte array to find.
 * @return The starting index of the byte array to find, or -1 if not found.
 */
private fun ByteArray.indexOf(element: ByteArray): Int {
    for (i in this.indices) {
        if (this[i] == element[0]) {
            var found = true
            for (j in element.indices) {
                if (this[i + j] != element[j]) {
                    found = false
                    break
                }
            }
            if (found) {
                return i
            }
        }
    }
    return -1
}
