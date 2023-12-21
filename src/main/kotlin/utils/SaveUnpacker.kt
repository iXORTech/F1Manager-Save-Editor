package utils

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Paths

import extensions.*

/*
 * SaveUnpacker.kt
 * F1Manager-Save-Editor
 *
 * Credit to https://github.com/xAranaktu/F1-Manager-2022-SaveFile-Repacker
 * for implementations of this class.
 *
 * Created by Qian Qian "Cubik" on Tuesday Dec. 19.
 *
 * Modified by Qian Qian "Cubik" on Tuesday Dec. 19.
 * - [ADD][feature] Backup prompt and backup functionality.
 * - [ADD][feature] Unpack the chunk1 data (data that is not a part of the real database) from the save file.
 * - [ADD][feature] Unpack the database from the save file.
 */

// Constants for unpacked file names.
private const val CHUNK1_NAME = "chunk1"
private const val MAIN_DB_NAME = "main.db"
private const val BACKUP_DB_NAME = "backup1.db"
private const val BACKUP_DB2_NAME = "backup2.db"

/**
 * Utility class to unpack the F1 Manager save file.
 */
class SaveUnpacker {
    companion object {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Unpack the F1 Manager save file.
         * @param save The save file to unpack.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun unpackSave(save: File) {
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
            val decompressedDatabase = Zlib.decompress(
                saveData.sliceArray(databaseOffset + 16 until saveData.size)
            ).toUByteArray()
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
}
