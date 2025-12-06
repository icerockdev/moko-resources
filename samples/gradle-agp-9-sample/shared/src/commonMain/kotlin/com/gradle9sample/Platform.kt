package com.gradle9sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform