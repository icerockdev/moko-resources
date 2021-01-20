package dev.icerock.moko.resources

actual class PluralsResource(
    val key: String,
    val numberFormat: Map<String, List<Pair<Double, String>>>
)