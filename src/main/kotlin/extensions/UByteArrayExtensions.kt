package extensions

/*
 * UByteArrayExtensions.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Thursday Dec. 21.
 */

@ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
fun UByteArray.toHexString() = "\\x" + joinToString("\\x") {
    it.toString(16).uppercase().padStart(2, '0')
}
