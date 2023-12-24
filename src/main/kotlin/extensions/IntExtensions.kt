package extensions

/*
 * IntExtensions.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Sunday Dec. 24.
 */


/**
 * Convert the integer to a ByteArray of size 4.
 * @param offset The offset to write the integer to.
 */
fun Int.toByteArray(offset: Int = 0): ByteArray {
    val buffer = ByteArray(4)
    buffer[offset + 0] = (this shr 0).toByte()
    buffer[offset + 1] = (this shr 8).toByte()
    buffer[offset + 2] = (this shr 16).toByte()
    buffer[offset + 3] = (this shr 24).toByte()
    return buffer
}
