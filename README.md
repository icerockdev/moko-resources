# About

This is a fork of https://github.com/icerockdev/moko-resources
with this change https://github.com/icerockdev/moko-resources/pull/233

## Assets use case

Put your files to MR/assets/ and then access them by MR.assets.folder1.folder2.my_file The only
difference between raw files and assets is that assets can be structured by folder.

And you can look up for asset by name. MR.assets.getAssetByFilePath(filePath = "your file path")

+ symbol is reserved for ios path delimiter, so avoid using it in folder/file names.

Assets as well as files have out of box implementation function for read text files from common code

- `readText()`

Usage on Android:

```
val text = MR.assets.test.getText(context = this)
```

Usage on Apple:

```
val text = MR.assets.test.readText()
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