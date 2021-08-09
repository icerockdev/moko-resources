package dev.icerock.moko.resources

expect object AssetResource {
    fun getPlatformPath(path: String): String
}

internal object AssetResourceHelper {
    fun getPlatformPath(path: String, pathIntoOneName: Boolean): String {

        val checkedPath: String = if (path[0] == '/')
            path.substring(1)
        else
            path

        return if (pathIntoOneName) checkedPath.replace('/', '_') else checkedPath
    }
}