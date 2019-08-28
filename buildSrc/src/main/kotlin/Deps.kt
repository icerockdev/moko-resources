object Deps {
    object Plugins {
        const val android =
            "com.android.tools.build:gradle:${Versions.Plugins.android}"
        const val kotlin =
            "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Plugins.kotlin}"
        const val androidExtensions =
            "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.Plugins.androidExtensions}"
    }

    object Libs {
        object Android {
            val kotlinStdLib = AndroidLibrary(
                name = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
            )
            val appCompat = AndroidLibrary(
                name = "androidx.appcompat:appcompat:${Versions.Libs.Android.appCompat}"
            )
        }

        object MultiPlatform {
            val kotlinStdLib = MultiPlatformLibrary(
                android = Android.kotlinStdLib.name,
                common = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
            )
            val mokoResources = MultiPlatformLibrary(
                common = "dev.icerock.moko:resources:${Versions.Libs.MultiPlatform.mokoResources}",
                iosX64 = "dev.icerock.moko:resources-iosx64:${Versions.Libs.MultiPlatform.mokoResources}",
                iosArm64 = "dev.icerock.moko:resources-iosarm64:${Versions.Libs.MultiPlatform.mokoResources}"
            )
        }
    }

    val plugins: Map<String, String> = mapOf(
        "com.android.application" to Plugins.android,
        "com.android.library" to Plugins.android,
        "org.jetbrains.kotlin.multiplatform" to Plugins.kotlin,
        "kotlin-kapt" to Plugins.kotlin,
        "kotlin-android" to Plugins.kotlin,
        "kotlin-android-extensions" to Plugins.androidExtensions
    )
}