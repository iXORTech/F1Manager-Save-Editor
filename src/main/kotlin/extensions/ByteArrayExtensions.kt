package extensions

/*
 * ByteArrayExtensions.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Thursday Dec. 21.
 */

/**
 * Get the index of a byte array in another byte array.
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
