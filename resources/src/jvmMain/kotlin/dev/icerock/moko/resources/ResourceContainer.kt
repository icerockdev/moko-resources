package dev.icerock.moko.resources

actual interface ResourceContainer<T>

actual fun ResourceContainer<ImageResource>.getImageByFileName(
    fileName: String
): ImageResource? = ImageResource("images/$fileName")