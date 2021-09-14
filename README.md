# About

This is a fork of https://github.com/icerockdev/moko-resources
with this change https://github.com/icerockdev/moko-resources/pull/233

## Assets use case

Put your files to MR/assets/ and then access them by MR.assets.folder1.folder2.my_file The only
difference between raw files and assets is that assets can be structured by folder.

And you can look up for asset by name. MR.assets.getAssetByFilePath(filePath = "your file path")

Plus symbol is reserved for ios path delimiter, so avoid using it in folder/file names.

Assets as well as files have out of box implementation function for read text files from common code

- `readText()`

Usage on Android:

```kotlin
val text = MR.assets.test.getText(context = this)
```

Usage on Apple:

```kotlin
val text = MR.assets.test.readText()
```

## Installation

For local publishing run in terminal the following commands:

```text
./gradlew -p resources-generator build publishToMavenLocal
./gradlew -p resources build publishToMavenLocal
```

root build.gradle

```groovy
buildscript {
 repositories {
  mavelLocal()
 }

    dependencies {
        classpath "com.github.krottv:resources-generator:0.17.2-assets3"
    }
}

```

For every project where Moko Resource is used provide:

```groovy
repositories {
    mavenLocal()
}
```

project build.gradle

```groovy
apply plugin: "com.github.krottv.multiplatform-resources"

dependencies {
    commonMainApi("com.github.krottv:moko-resources:0.17.2-assets3")
}

multiplatformResources {
    multiplatformResourcesPackage = "org.example.library" // required
    iosBaseLocalizationRegion = "en" // optional, default "en"
    multiplatformResourcesSourceSet = "commonClientMain"  // optional, default "commonMain"
}
```