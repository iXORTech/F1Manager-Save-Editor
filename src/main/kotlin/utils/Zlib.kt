package utils

import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

import extensions.*

/*
 * Zlib.kt
 * F1Manager-Save-Editor
 *
 * Credit to https://gist.github.com/marcouberti/40dbbd836562b35ace7fb2c627b0f34f
 * for implementations of this class.
 *
 * Created by Qian Qian "Cubik" on Tuesday Dec. 19.
 *
 * Modified by Qian Qian "Cubik" on Tuesday Dec. 19.
 * - [ADD][feature] Add ZLIB compression and decompression.
 *
 * * Modified by Qian Qian "Cubik" on Thursday Dec. 21.
 * - [ADD][chore] Add logging.
 */

class Zlib {
    companion object {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Compress a byte array using ZLIB.
         * @param input The byte array to compress.
         * @return The compressed byte array.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun compress(input: ByteArray): ByteArray {
            logger.debug("Compressing ${input.size} bytes of data.")
            logger.trace("Compressing ByteArray ${input.toHexString()}")

            // Compress the bytes
            // 1 to 4 bytes/char for UTF-8
            val output = ByteArray(input.size * 4)
            val compressor = Deflater().apply {
                setInput(input)
                finish()
            }
            val compressedDataLength: Int = compressor.deflate(output)
            return output.copyOfRange(0, compressedDataLength)
        }

        /**
         * Decompress a byte array using ZLIB.
         * @param input The byte array to decompress.
         * @return The decompressed byte array.
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun decompress(input: ByteArray): ByteArray {
            logger.debug("Decompressing ${input.size} bytes of data.")
            logger.trace("Decompressing ByteArray ${input.toHexString()}")

            val inflater = Inflater()
            val outputStream = ByteArrayOutputStream()

            return outputStream.use {
                val buffer = ByteArray(1024)

                inflater.setInput(input)

                var count = -1
                while (count != 0) {
                    count = inflater.inflate(buffer)
                    outputStream.write(buffer, 0, count)
                }

                inflater.end()
                outputStream.toByteArray()
            }
        }
    }
}
