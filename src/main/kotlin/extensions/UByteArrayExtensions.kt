package extensions

/*
 * UByteArrayExtensions.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Thursday Dec. 21.
 */

/**
 * Convert the unsigned byte array to a hex string.
 * Each byte is represented by '\x' followed by two uppercase hexadecimal digits.
 * @return The hex string representation of the unsigned byte array.
 */
@ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
fun UByteArray.toHexString() = "\\x" + joinToString("\\x") {
    it.toString(16).uppercase().padStart(2, '0')
}
