package utils

import exceptions.UnpackedSaveNotFoundException
import java.io.File
import java.nio.file.Paths

/*
 * SaveRepacker.kt
 * F1Manager-Save-Editor
 *
 * Credit to https://github.com/xAranaktu/F1-Manager-2022-SaveFile-Repacker
 * for implementations of this class.
 *
 * Created by Qian Qian "Cubik" on Tuesday Dec. 19.
 *
 * Modified by Qian Qian "Cubik" on Tuesday Dec. 19.
 * - [ADD][feature] Repack the save file.
 */

// Constants for unpacked file names.
private const val CHUNK1_NAME = "chunk1"
private const val MAIN_DB_NAME = "main.db"
private const val BACKUP_DB_NAME = "backup1.db"
private const val BACKUP_DB2_NAME = "backup2.db"

/**
 * Utility class to repack unpacked database and other data into a F1 Manager save file.
 */
class SaveRepacker {
    companion object {
        /**
         * Write an integer to a ByteArray.
         * @param offset The offset to write the integer to.
         * @param data The integer to write.
         */
        private fun write4BytesToBuffer(offset: Int, data: Int): ByteArray {
            val buffer = ByteArray(4)
            buffer[offset + 0] = (data shr 0).toByte()
            buffer[offset + 1] = (data shr 8).toByte()
            buffer[offset + 2] = (data shr 16).toByte()
            buffer[offset + 3] = (data shr 24).toByte()
            return buffer
        }

        /**
         * Get the compressed byte data of the database.
         * @param databaseDatas The database data to compress.
         * @return The compressed database data.
         */
        private fun getCompressedDatabase(databaseDatas: List<ByteArray>): List<ByteArray> {
            val data: MutableList<ByteArray> = mutableListOf()

            val dbSizes: MutableList<Int> = mutableListOf()
            var dataToCompress = byteArrayOf()

            for (databaseData in databaseDatas) {
                dbSizes.add(databaseData.size)
                dataToCompress = dataToCompress.plus(databaseData)
            }

            val compressedData = Zlib.compress(dataToCompress)

            data.add(write4BytesToBuffer(0, compressedData.size))
            for (i in dbSizes.indices) {
                data.add(write4BytesToBuffer(0, dbSizes[i]))
            }
            data.add(compressedData)

            return data
        }

        /**
         * Repack the F1 Manager save file.
         * @param target The target directory to save the repacked save file.
         * @param name The name of the repacked save file.
         */
        fun repackSave(target: String, name: String) {
            println("Repacking save file...")

            // Check if the unpacked save files exist.
            val pwd = Paths.get("").toAbsolutePath().toString().plus("/unpacked_save/")
            if (!File(pwd).exists()) {
                throw UnpackedSaveNotFoundException()
            }

            // Get the unpacked save files.
            val chunk1File = File(pwd + CHUNK1_NAME)
            val mainDbFile = File(pwd + MAIN_DB_NAME)
            val backupDbFile = File(pwd + BACKUP_DB_NAME)
            val backupDb2File = File(pwd + BACKUP_DB2_NAME)

            // Get the new save data.
            var newSaveData: ByteArray = byteArrayOf()

            newSaveData = if (chunk1File.exists()) {
                newSaveData.plus(chunk1File.readBytes())
            } else {
                throw UnpackedSaveNotFoundException()
            }

            val mainDbData = if (mainDbFile.exists()) {
                mainDbFile.readBytes()
            } else {
                throw UnpackedSaveNotFoundException()
            }

            val backupDbData = if (backupDbFile.exists()) {
                backupDbFile.readBytes()
            } else {
                byteArrayOf()
            }

            val backupDb2Data = if (backupDb2File.exists()) {
                backupDb2File.readBytes()
            } else {
                byteArrayOf()
            }

            // Compress the new save data.
            val compressedDatabase = getCompressedDatabase(
                listOf(
                    mainDbData,
                    backupDbData,
                    backupDb2Data
                )
            )

            for (database in compressedDatabase) {
                newSaveData += database
            }

            // Write new save data to save file.
            val saveFile = File(target, name)
            if (saveFile.exists()) {
                saveFile.delete()
            }
            saveFile.writeBytes(newSaveData)

            println("Save file repacked!")
        }
    }
}
