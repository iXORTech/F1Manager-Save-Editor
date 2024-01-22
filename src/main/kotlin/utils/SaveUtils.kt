package utils

import exceptions.UnpackedSaveNotFoundException
import extensions.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Paths

/*
 * SaveUtils.kt
 * F1Manager-Save-Editor
 *
 * Credit to https://github.com/xAranaktu/F1-Manager-2022-SaveFile-Repacker
 * for implementations of the unpacker and repacker.
 *
 * Created by Qian Qian "Cubik" on Monday Jan. 22.
 */


// Constants for unpacked file names.
private const val CHUNK1_NAME = "chunk1"
private const val MAIN_DB_NAME = "main.db"
private const val BACKUP_DB_NAME = "backup1.db"
private const val BACKUP_DB2_NAME = "backup2.db"

class SaveUtils(val save: File) {
    /**
     * Utility class to unpack the F1 Manager save file.
     */
    inner class SaveUnpacker {
        private val logger = LoggerFactory.getLogger(SaveUnpacker::class.java)

        /**
         * Unpack the F1 Manager save file.
         * @param save The save file to unpack.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun unpackSave() {
            // Get the directory to save the unpacked files.
            val pwd = Paths.get("").toAbsolutePath().toString().plus("/unpacked_save/")
            File(pwd).delete()
            File(pwd).mkdir()

            logger.debug("Unpacking save file from ${save.absolutePath} to $pwd.")

            // Backup the save file.
            println("Make sure you have a backup of your save file before proceeding!")
            print("Do you want the program to make a backup of your save file? (y/n) ")
            val backup = readln().lowercase() == "y"
            if (backup) {
                logger.debug("Making backup of save file ${save.absolutePath}.")
                println("Making backup...")
                val backupFile = File(save.absolutePath + ".bak")
                if (backupFile.exists()) {
                    backupFile.delete()
                }
                save.copyTo(backupFile)
                println("Backup complete!")
                logger.debug("Backup complete!")
            } else {
                println("Skipping backup...")
                logger.debug("Skipping backup due to user option.")
            }

            println("Unpacking save file...")

            // Read the save file.
            val saveData = save.readBytes()
            logger.debug("Read ${saveData.size} bytes from save file.")

            // Signature before the packed database.
            // \x00\x05\x00\x00\x00\x4E\x6F\x6E\x65\x00\x05\x00\x00\x00\x4E\x6F\x6E\x65\x00
            val preDatabaseSig = byteArrayOf(
                0x00, 0x05, 0x00, 0x00, 0x00, 0x4E, 0x6F, 0x6E, 0x65,
                0x00, 0x05, 0x00, 0x00, 0x00, 0x4E, 0x6F, 0x6E, 0x65, 0x00
            )

            // Get the offset of the packed database.
            val databaseOffset = saveData.indexOf(preDatabaseSig) + preDatabaseSig.size + 4
            logger.debug("databaseOffset = $databaseOffset")

            // Save to part of the save that is not the database for repacking.
            val chunk1File = File(pwd, CHUNK1_NAME)
            if (chunk1File.exists()) {
                chunk1File.delete()
            }
            val chunk1Data = saveData.sliceArray(0 until databaseOffset)
            logger.debug("Writing ${chunk1Data.size} bytes to ${chunk1File.absolutePath}.")
            logger.trace("chunk1Data = ${chunk1Data.toHexString()}")
            chunk1File.writeBytes(chunk1Data)

            // Get ZLIB size.
            val zlibSize = ByteBuffer.wrap(
                saveData.sliceArray(databaseOffset until databaseOffset + 4)
            ).order(ByteOrder.LITTLE_ENDIAN).int
            logger.debug("zlibSize = $zlibSize")

            // Get the File object and the size of each database.
            val databases = mapOf(
                File(pwd, MAIN_DB_NAME) to ByteBuffer.wrap(
                    saveData.sliceArray(
                        databaseOffset + 4 until databaseOffset + 8
                    )
                ).order(ByteOrder.LITTLE_ENDIAN).int,
                File(pwd, BACKUP_DB_NAME) to ByteBuffer.wrap(
                    saveData.sliceArray(
                        databaseOffset + 8 until databaseOffset + 12
                    )
                ).order(ByteOrder.LITTLE_ENDIAN).int,
                File(pwd, BACKUP_DB2_NAME) to ByteBuffer.wrap(
                    saveData.sliceArray(
                        databaseOffset + 12 until databaseOffset + 16
                    )
                ).order(ByteOrder.LITTLE_ENDIAN).int
            )

            for ((databaseFile, databaseSize) in databases) {
                logger.debug("database file = ${databaseFile.absolutePath}, size = $databaseSize")
            }

            // Get the database data and decompress it.
            val decompressedDatabase = saveData
                .sliceArray(databaseOffset + 16 until saveData.size)
                .decompress().toUByteArray()
            logger.debug("database decompressed. size = ${decompressedDatabase.size}")

            // Write the decompressed database to the files.
            var idx = 0
            for ((databaseFile, databaseSize) in databases) {
                if (databaseSize == 0) {
                    continue
                }

                if (databaseFile.exists()) {
                    databaseFile.delete()
                }

                val databaseData = decompressedDatabase.sliceArray(idx until idx + databaseSize)
                logger.debug("Writing ${databaseData.size} ($databaseSize) bytes to ${databaseFile.absolutePath}.")
                logger.trace("databaseData = ${databaseData.toHexString()}")
                databaseFile.writeBytes(databaseData.toByteArray())

                idx += databaseSize
            }

            println("Unpacking save file complete!")
            logger.debug("Successfully unpacked save file ${save.absolutePath} to $pwd.")
        }
    }

    /**
     * Utility class to repack unpacked database and other data into a F1 Manager save file.
     */
    inner class SaveRepacker {
        private val logger = LoggerFactory.getLogger(SaveRepacker::class.java)

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
        fun repackSave() {
            val repackedSaveFile = save.parentFile.absolutePath +
                    "/${save.name.substring(0 until save.name.lastIndexOf('.'))}.repacked.sav"

            logger.debug("Repacking save file to $repackedSaveFile.")
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
            val saveFile = File(repackedSaveFile)
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
