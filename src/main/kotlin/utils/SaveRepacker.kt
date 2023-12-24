package utils

import exceptions.UnpackedSaveNotFoundException
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

import extensions.*

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
 *
 * Modified by Qian Qian "Cubik" on Thursday Dec. 21.
 * - [ADD][chore] Add logging.
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
        private val logger = LoggerFactory.getLogger(SaveUnpacker::class.java)

        /**
         * Get the compressed byte data of the database.
         * @param databaseDatas The database data to compress.
         * @return The compressed database data.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        private fun getCompressedDatabase(databaseDatas: List<ByteArray>): List<ByteArray> {
            logger.debug("Compressing database data.")
            val data: MutableList<ByteArray> = mutableListOf()

            val dbSizes: MutableList<Int> = mutableListOf()
            var dataToCompress = byteArrayOf()

            for (databaseData in databaseDatas) {
                val size = databaseData.size
                logger.debug("Compressing database data of size $size.")
                logger.trace("databaseData = ${databaseData.toHexString()}")
                dbSizes.add(size)
                dataToCompress = dataToCompress.plus(databaseData)
            }

            val compressedData = dataToCompress.compress()
            logger.debug("Compressed database data of size ${dataToCompress.size} to ${compressedData.size}.")
            logger.trace("compressedData = ${compressedData.toHexString()}")

            data.add(compressedData.size.toByteArray())
            for (dbSize in dbSizes) {
                data.add(dbSize.toByteArray())
            }
            data.add(compressedData)

            return data
        }

        /**
         * Repack the F1 Manager save file.
         * @param target The target directory to save the repacked save file.
         * @param name The name of the repacked save file.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun repackSave(target: String, name: String) {
            logger.debug("Repacking save file to $target/$name.")
            println("Repacking save file...")

            // Check if the unpacked save files exist.
            logger.debug("Checking if unpacked save files exist.")
            val pwd = Paths.get("").toAbsolutePath().toString().plus("/unpacked_save/")
            if (!File(pwd).exists()) {
                throw UnpackedSaveNotFoundException()
            }
            logger.debug("Unpacked save files found at $pwd.")

            // Get the unpacked save files.
            val chunk1File = File(pwd + CHUNK1_NAME)
            val mainDbFile = File(pwd + MAIN_DB_NAME)
            val backupDbFile = File(pwd + BACKUP_DB_NAME)
            val backupDb2File = File(pwd + BACKUP_DB2_NAME)

            // Get the new save data.
            var newSaveData: ByteArray = byteArrayOf()

            newSaveData = if (chunk1File.exists()) {
                val chunk1Data = chunk1File.readBytes()
                logger.debug("Read ${chunk1Data.size} bytes from $CHUNK1_NAME.")
                logger.trace("chunk1Data = ${chunk1Data.toHexString()}")
                newSaveData.plus(chunk1Data)
            } else {
                throw UnpackedSaveNotFoundException()
            }

            val mainDbData = if (mainDbFile.exists()) {
                val data = mainDbFile.readBytes()
                logger.debug("Read ${data.size} bytes from $MAIN_DB_NAME.")
                logger.trace("mainDbData = ${data.toHexString()}")
                data
            } else {
                throw UnpackedSaveNotFoundException()
            }

            val backupDbData = if (backupDbFile.exists()) {
                val data = backupDbFile.readBytes()
                logger.debug("Read ${data.size} bytes from $BACKUP_DB_NAME.")
                logger.trace("backupDbData = ${data.toHexString()}")
                data
            } else {
                logger.debug("No $BACKUP_DB_NAME found. Write empty ByteArray.")
                byteArrayOf()
            }

            val backupDb2Data = if (backupDb2File.exists()) {
                val data = backupDb2File.readBytes()
                logger.debug("Read ${data.size} bytes from $BACKUP_DB2_NAME.")
                logger.trace("backupDb2Data = ${data.toHexString()}")
                data
            } else {
                logger.debug("No $BACKUP_DB2_NAME found. Write empty ByteArray.")
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
            logger.debug("New Save Data Generated. size = ${newSaveData.size}")
            logger.trace("newSaveData = ${newSaveData.toHexString()}")

            // Write new save data to save file.
            val saveFile = File(target, name)
            if (saveFile.exists()) {
                saveFile.delete()
            }
            saveFile.writeBytes(newSaveData)
            logger.debug("Wrote ${newSaveData.size} bytes to ${saveFile.absolutePath}.")

            println("Save file repacked!")
            logger.debug("Successfully repacked save file to ${saveFile.absolutePath}.")
        }
    }
}
