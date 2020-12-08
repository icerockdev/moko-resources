package dev.icerock.moko.resources

actual fun ResourceContainer<ImageResource>.getImageByFileName(fileName: String): ImageResource? {
    return ImageResource(fileName, nsBundle).let { imgRes ->
        if (imgRes.toNSImage() != null) {
            imgRes
        } else {
            null
        }
    }
}
