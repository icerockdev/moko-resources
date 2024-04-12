package app.kotlin2sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform