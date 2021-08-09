package dev.icerock.moko.resources

expect object AssetResource {
    fun getPlatformPath(path: String): String
}