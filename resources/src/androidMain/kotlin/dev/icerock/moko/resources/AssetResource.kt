package dev.icerock.moko.resources

actual object AssetResource {
    actual fun getPlatformPath(path: String): String {
        return if (path[0] == '/')
            path.substring(1)
        else
            path
    }
}