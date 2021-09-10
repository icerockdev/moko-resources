![moko-resources](img/logo.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/resources) ](https://repo1.maven.org/maven2/dev/icerock/moko/resources) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=resources)
![badge][badge-android]
![badge][badge-iosx64]
![badge][badge-iosarm64]
![badge][badge-macos64]
![badge][badge-jvm]

# Mobile Kotlin resources

This is a fork of https://github.com/icerockdev/moko-resources
with this change https://github.com/icerockdev/moko-resources/pull/233

## Assets use case

Put your files to MR/assets/ and then access them by MR.assets.folder1.folder2.my_file The only
difference between raw files and assets is that assets can be structured by folder.

And you can look up for asset by name. MR.assets.getAssetByFilePath(filePath = "your file path")

Assets as well as files have out of box implementation function for read text files from common code
- `readText()`

Usage on Android:

```
val text = MR.files.test.getText(context = this)
```

Usage on Apple:

```
val text = MR.files.test.readText()
```

## Installation

root build.gradle

```groovy
buildscript {
 repositories {
  gradlePluginPortal()
 }

 dependencies {
  classpath "github.vkrot:resources-generator:0.17.2-assets"
 }
}


allprojects {
    repositories {
        mavenCentral()
    }
}
```

project build.gradle
```groovy
apply plugin: "github.vkrot.multiplatform-resources"

dependencies {
 commonMainApi("github.vkrot:resources:0.17.2-assets")
}

multiplatformResources {
    multiplatformResourcesPackage = "org.example.library" // required
    iosBaseLocalizationRegion = "en" // optional, default "en"
    multiplatformResourcesSourceSet = "commonClientMain"  // optional, default "commonMain"
}
```