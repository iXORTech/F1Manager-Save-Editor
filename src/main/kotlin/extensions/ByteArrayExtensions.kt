package extensions

import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

/*
 * ByteArrayExtensions.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Thursday Dec. 21.
 */

class ByteArrayExtensions

private val byteArrayLogger = LoggerFactory.getLogger(ByteArrayExtensions::class.java)

/**
 * Get the index of another byte array in the byte array.
 * @param element The byte array to find.
 * @return The starting index of the byte array to find, or -1 if not found.
 */
fun ByteArray.indexOf(element: ByteArray): Int {
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

/**
 * Convert the byte array to a hex string.
 * Each byte is represented by '\x' followed by two uppercase hexadecimal digits.
 * @return The hex string representation of the byte array.
 */
@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = "\\x" + asUByteArray().joinToString("\\x") {
    it.toString(16).uppercase().padStart(2, '0')
}

/**
 * Compress the byte array using ZLIB.
 *
 * Credit to https://gist.github.com/marcouberti/40dbbd836562b35ace7fb2c627b0f34f for the implementation.
 *
 * @return The compressed byte array.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.compress(): ByteArray {
    byteArrayLogger.debug("Compressing ${this.size} bytes of data.")
    byteArrayLogger.trace("Compressing ByteArray ${this.toHexString()}")

    // Compress the bytes
    // 1 to 4 bytes/char for UTF-8
    val input = this
    val output = ByteArray(this.size * 4)
    val compressor = Deflater().apply {
        setInput(input)
        finish()
    }
    val compressedDataLength: Int = compressor.deflate(output)
    return output.copyOfRange(0, compressedDataLength)
}

/**
 * Decompress the byte array using ZLIB.
 *
 * Credit to https://gist.github.com/marcouberti/40dbbd836562b35ace7fb2c627b0f34f for the implementation.
 *
 * @return The decompressed byte array.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.decompress(): ByteArray {
    byteArrayLogger.debug("Decompressing ${this.size} bytes of data.")
    byteArrayLogger.trace("Decompressing ByteArray ${this.toHexString()}")

    val inflater = Inflater()
    val outputStream = ByteArrayOutputStream()

    return outputStream.use {
        val buffer = ByteArray(1024)

        inflater.setInput(this)

        var count = -1
        while (count != 0) {
            count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        inflater.end()
        outputStream.toByteArray()
    }
}

