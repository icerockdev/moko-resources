package dev.icerock.moko.resources

actual object AssetResource {
    actual fun getPlatformPath(path: String): String {
        return AssetResourceHelper.getPlatformPath(path, pathIntoOneName = false)
    }
}