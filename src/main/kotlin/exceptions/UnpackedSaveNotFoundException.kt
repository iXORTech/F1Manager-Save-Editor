package exceptions

/*
 * UnpackedSaveNotFoundException.kt
 * F1Manager-Save-Editor
 *
 * Created by Qian Qian "Cubik" on Tuesday Dec. 19.
 */

class UnpackedSaveNotFoundException : Exception() {
    override val message: String?
        get() = "Cannot find unpacked save file under the current directory."
}