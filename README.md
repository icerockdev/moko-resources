![moko-resources](img/logo.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/resources) ](https://repo1.maven.org/maven2/dev/icerock/moko/resources) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=resources)
![badge][badge-android]
![badge][badge-iosx64]
![badge][badge-iosarm64]
![badge][badge-iosSimulatorArm64]
![badge][badge-macos64]
![badge][badge-jvm]

# Mobile Kotlin resources
This is a Kotlin MultiPlatform library that provides access to the resources on iOS & Android with the support of the default system localization.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Versions](#versions)
- [Installation](#installation)
- [Usage](#usage)
- [Samples](#samples)
- [Set Up Locally](#set-up-locally)
- [Contributing](#contributing)
- [License](#license)

## Features
- **Strings, Plurals, Images, Fonts, Files** to access the corresponding resources from common code;
- **Colors** with light/dark mode support;
- **StringDesc** for lifecycle-aware access to resources and unified localization on both platforms;
- **Static** iOS frameworks support;
- **FatFrameworkWithResourcesTask** Gradle task.

## Requirements
- Gradle version 6.8.3+
- Android API 16+
- iOS version 11.0+

## Installation
root build.gradle
```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath "dev.icerock.moko:resources-generator:0.17.4"
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
apply plugin: "dev.icerock.mobile.multiplatform-resources"

dependencies {
    commonMainApi("dev.icerock.moko:resources:0.17.4")
}

multiplatformResources {
    multiplatformResourcesPackage = "org.example.library" // required
    iosBaseLocalizationRegion = "en" // optional, default "en"
    multiplatformResourcesSourceSet = "commonClientMain"  // optional, default "commonMain"
}
```

If your project includes a build type, for example `staging` which isn't in moko-resources. That isn't an issue. Use matchingFallbacks to specify alternative matches for a given build type, as shown below
```
buildTypes {
    staging {
        initWith debug
        matchingFallbacks = ['debug']
    }
}
```

ios-app Info.plist:
```xml
<key>CFBundleLocalizations</key>
<array>
<string>en</string>
<string>ru</string>
</array>
```
in array should be added all used languages.

### Static kotlin frameworks support
If project configured with static framework output (for example by `org.jetbrains.kotlin.native.cocoapods` plugin)
in Xcode project should be added `Build Phase` (at end of list) with script:
```shell script
"$SRCROOT/../gradlew" -p "$SRCROOT/../" :yourframeworkproject:copyFrameworkResourcesToApp \
    -Pmoko.resources.PLATFORM_NAME=$PLATFORM_NAME \
    -Pmoko.resources.CONFIGURATION=$CONFIGURATION \
    -Pmoko.resources.BUILT_PRODUCTS_DIR=$BUILT_PRODUCTS_DIR \
    -Pmoko.resources.CONTENTS_FOLDER_PATH=$CONTENTS_FOLDER_PATH
```
Please replace `:yourframeworkproject` to kotlin project gradle path, and set correct relative path (`$SRCROOT/../` in example).  
This phase will copy resources into application, because static frameworks can't have resources.

To disable warnings about static framework in gradle set flag:
```kotlin
multiplatformResources {
    disableStaticFrameworkWarning = true
}
```

#### With Pods dependencies in Kotlin
When you use `org.jetbrains.kotlin.native.cocoapods` plugin and also kotlin module depends to Pods -
you also need to pass extra properties:
```shell script
"$SRCROOT/../gradlew" -p "$SRCROOT/../" :shared:copyFrameworkResourcesToApp \
    -Pmoko.resources.PLATFORM_NAME=$PLATFORM_NAME \
    -Pmoko.resources.CONFIGURATION=$CONFIGURATION \
    -Pmoko.resources.BUILT_PRODUCTS_DIR=$BUILT_PRODUCTS_DIR \
    -Pmoko.resources.CONTENTS_FOLDER_PATH=$CONTENTS_FOLDER_PATH\
    -Pkotlin.native.cocoapods.target=$KOTLIN_TARGET \
    -Pkotlin.native.cocoapods.configuration=$CONFIGURATION \
    -Pkotlin.native.cocoapods.cflags="$OTHER_CFLAGS" \
    -Pkotlin.native.cocoapods.paths.headers="$HEADER_SEARCH_PATHS" \
    -Pkotlin.native.cocoapods.paths.frameworks="$FRAMEWORK_SEARCH_PATHS"
```
and setup extra build settings in your xcode target:
```
'KOTLIN_TARGET[sdk=iphonesimulator*]' => 'ios_x64'
'KOTLIN_TARGET[sdk=iphoneos*]' => 'ios_arm64'
```
[here example of changes](https://github.com/ln-12/moko-resources-issue-99/pull/2/files)

## Usage
### Example 1 - simple localization string
The first step is a create a file `strings.xml` in `commonMain/resources/MR/base` with the following content:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string">My default localization string</string>
</resources>
```
Next - create a file `strings.xml` with localized strings in `commonMain/resource/MR/<languageCode>`. Here's an example of creating `commonMain/resource/MR/ru` for a Russian localization:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string">Моя строка локализации по умолчанию</string>
</resources>
```
After adding the resources we can call a gradle sync or execute a gradle task `generateMRcommonMain`. This will generate a `MR` class containing `MR.strings.my_string`, which we can use in `commonMain`:
```kotlin
fun getMyString(): StringDesc {
    return StringDesc.Resource(MR.strings.my_string)
}
``` 
After this we can use our functions on the platform side:  
Android:
```kotlin
val string = getMyString().toString(context = this)
```
iOS:
```swift
let string = getMyString().localized()
```
Note: `StringDesc` is a multiple-source container for Strings: in StringDesc we can use a resource, plurals, formatted variants, or raw string. To convert `StringDesc` to `String` on Android call `toString(context)` (a context is required for the resources usage), on iOS - call `localized()`.

#### MR directly from native side
Android:
```kotlin
val string = MR.strings.my_string.desc().toString(context = this)
``` 
iOS:
```swift
let string = MR.strings().my_string.desc().localized()
```

#### Get resourceId for Jetpack Compose / SwiftUI
Android:
```kotlin
val resId = MR.strings.my_string.resourceId
```
for example in Compose:
```kotlin
text = stringResource(id = MR.strings.email.resourceId)
```

iOS:
```swift
LocalizedStringKey(MR.strings().email.resourceId)
```

Note: more info in issue [#126](https://github.com/icerockdev/moko-resources/issues/126).

### Example 2 - formatted localization string
In `commonMain/resources/MR/base/strings.xml` add:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string_formatted">My format '%s'</string>
</resources>
```
Then add the localized values for other languages like in example #1.
Now create the following function in `commonMain`:
```kotlin
fun getMyFormatDesc(input: String): StringDesc {
    return StringDesc.ResourceFormatted(MR.strings.my_string_formatted, input)
}
```
To create formatted strings from resources you can also use extension `format`:
```kotlin
fun getMyFormatDesc(input: String): StringDesc {
    return MR.strings.my_string_formatted.format(input)
}
```
Now add support on the platform side like in example #1:  
Android:
```kotlin
val string = getMyFormatDesc("hello").toString(context = this)
```
iOS:
```swift
let string = getMyFormatDesc(input: "hello").localized()
```

### Example 3 - plural string
The first step is to create a file `plurals.xml` in `commonMain/resources/MR/base` with the following content:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <plural name="my_plural">
        <item quantity="zero">zero</item>
        <item quantity="one">one</item>
        <item quantity="two">two</item>
        <item quantity="few">few</item>
        <item quantity="many">many</item>
        <item quantity="other">other</item>
    </plural>
</resources>
```
Then add the localized values for other languages like in example #1.  
Next, create a function in `commonMain`:
```kotlin
fun getMyPluralDesc(quantity: Int): StringDesc {
    return StringDesc.Plural(MR.plurals.my_plural, quantity)
}
```
Now add support on the platform side like in example #1:  
Android:
```kotlin
val string = getMyPluralDesc(10).toString(context = this)
```
iOS:
```swift
let string = getMyPluralDesc(quantity: 10).localized()
```

### Example 4 - plural formatted string
The first step is to create file `plurals.xml` in `commonMain/resources/MR/base` with the following content:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <plural name="my_plural">
        <item quantity="zero">no items</item>
        <item quantity="one">%d item</item>
        <item quantity="two">%d items</item>
        <item quantity="few">%d items</item>
        <item quantity="many">%d items</item>
        <item quantity="other">%d items</item>
    </plural>
</resources>
```
Then add the localized values for other languages like in example #1.  
Next, create a function in `commonMain`:
```kotlin
fun getMyPluralFormattedDesc(quantity: Int): StringDesc {
    // we pass quantity as selector for correct plural string and for pass quantity as argument for formatting
    return StringDesc.PluralFormatted(MR.plurals.my_plural, quantity, quantity)
}
```
To create formatted plural strings from resources you can also use extension `format`:
```kotlin
fun getMyPluralFormattedDesc(quantity: Int): StringDesc {
    // we pass quantity as selector for correct plural string and for pass quantity as argument for formatting
    return MR.plurals.my_plural.format(quantity, quantity)
}
```
And like in example #1, add the platform-side support:  
Android:
```kotlin
val string = getMyPluralFormattedDesc(10).toString(context = this)
```
iOS:
```swift
let string = getMyPluralFormattedDesc(quantity: 10).localized()
```

### Example 5 - pass raw string or resource
If we already use some resources as a placeholder value, we can use `StringDesc` to change the string source:
```kotlin
fun getUserName(user: User?): StringDesc {
    if(user != null) {
        return StringDesc.Raw(user.name)
    } else {
        return StringDesc.Resource(MR.strings.name_placeholder)
    }
}
```
And just like in example 1 usage on platform side:  
Android:
```kotlin
val string1 = getUserName(user).toString(context = this) // we got name from User model
val string2 = getUserName(null).toString(context = this) // we got name_placeholder from resources
```
iOS:
```swift
let string1 = getUserName(user: user).localized() // we got name from User model
let string2 = getUserName(user: null).localized() // we got name_placeholder from resources
```

### Example 6 - Select localization in runtime
You can force `StringDesc` to use preferred localization in common code:
```kotlin
StringDesc.localeType = StringDesc.LocaleType.Custom("es")
```
and return to system behaviour (when localization depends on device settings):
```kotlin
StringDesc.localeType = StringDesc.LocaleType.System()
```

### Example 7 - pass image
Image resources directory is `commonMain/resources/MR/images` with support of nested directories.  
Image name should be end with one of:
- `@0.75x` - android ldpi;
- `@1x` - android mdpi, ios 1x;
- `@1.5x` - android hdpi;
- `@2x` - android xhdpi, ios 2x;
- `@3x` - android xxhdpi, ios 3x;
- `@4x` - android xxxhdpi.
  Supported `png` and `jpg` resources for now.

If we add to `commonMain/resources/MR/images` files:
- `home_black_18@1x.png`
- `home_black_18@2x.png`

We got autogenerated `MR.images.home_black_18` `ImageResource` in code, that we can use:
- Android: `imageView.setImageResource(image.drawableResId)`
- iOS: `imageView.image = image.toUIImage()`

You can get images by their name too

in `commonMain` create a `Resources.kt` file with the content below
```kotlin
fun getImageByFileName(name: String): ImageResource {
    val fallbackImage = MR.images.transparent
    return MR.images.getImageByFileName(name) ?: fallbackImage
}
```

- Android: `imageView.setImageResource(getDrawableByFileName("image_name"))`
- iOS: `imageView.image = ResourcesKt.getDrawableByFileName(name: "image_name").toUIImage()!`

### Example 8 - pass font
Fonts resources directory is `commonMain/resources/MR/fonts`.  
Font name should be this pattern: `<fontFamily>-<fontStyle>` like:
- `Raleway-Bold.ttf`
- `Raleway-Regular.ttf`
- `Raleway-Italic.ttf`
  Supports `ttf` and `otf` resources.

If we add to `commonMain/resources/MR/fonts` files:
- `Raleway-Bold.ttf`
- `Raleway-Regular.ttf`
- `Raleway-Italic.ttf`

We got autogenerated `MR.fonts.Raleway.italic`, `MR.fonts.Raleway.regular`, `MR.fonts.Raleway.bold` `FontResource` in code, that we can use:
- Android: `textView.typeface = font.getTypeface(context = this)`
- iOS: `textView.font = font.uiFont(withSize: 14.0)`

### Example 9 - pass colors
Colors resources directory is `commonMain/resources/MR/colors`.  
Colors files is `xml` with format:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- format: #RRGGBB[AA] or 0xRRGGBB[AA] or RRGGBB[AA] where [AA] - optional -->
    <color name="valueColor">#B02743FF</color>
    <color name="referenceColor">@color/valueColor</color>
    <color name="themedColor">
        <light>0xB92743FF</light>
        <dark>7CCFEEFF</dark>
    </color>
    <color name="themedReferenceColor">
        <light>@color/valueColor</light>
        <dark>@color/referenceColor</dark>
    </color>
</resources>
```
If you want use one color without light/dark theme selection:
```xml
<color name="valueColor">#B02743FF</color>
```
If you want use value of other color - use references:
```xml
<color name="referenceColor">@color/valueColor</color>
```
If you want different colors in light/dark themes:
```xml
<color name="themedColor">
    <light>0xB92743FF</light>
    <dark>7CCFEEFF</dark>
</color>
```
Also themed colors can be referenced too:
```xml
<color name="themedReferenceColor">
    <light>@color/valueColor</light>
    <dark>@color/referenceColor</dark>
</color>
```

Colors available in common code insode `MR.colors.**` as `ColorResource`.  
`ColorResource` can be `ColorResource.Single` - simple color without theme selection.  
And can be `ColorResource.Themed` with colors for each mode.

You can read colors value from common code:
```kotlin
val color: Color = MR.colors.valueColor.color
```
but if you use `ColorResource.Themed` you can get current theme color only from platfrom side.
Android:
```kotlin
val color: Color = MR.colors.valueColor.getColor(context = this)
```
iOS:
```swift
val color: UIColor = MR.colors.valueColor.getColor(UIScreen.main.traitCollection.userInterfaceStyle)

// If your SwiftUI View can not handle the run time dark/light mode changes for colors
// add this line on top of the View it will make it aware of dark/light mode changes 
@Environment(\.colorScheme) var colorScheme
```

You can get Color from resource on IOS with toUIColor
For use it you should export moko-resources library to IOS
```
framework {
    export(libs.mokoResources)
}
```

### Gradle task for creating Fat Framework with resources

If you want to create Fat Framework for iOS with all resources from KMP Gradle module you should use
extended Gradle task `FatFrameworkWithResourcesTask`. There is example of
`FatFrameworkWithResourcesTask` task using for the `mpp-library` module of the Sample. In the end
of the `sample/mpp-library/build.gradle.kts` file:

```kotlin
tasks.register("debugFatFramework", dev.icerock.gradle.tasks.FatFrameworkWithResourcesTask::class) {
    baseName = "multiplatform"

    val targets = mapOf(
        "iosX64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosX64"),
        "iosArm64" to kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("iosArm64")
    )

    from(
        targets.toList().map {
            it.second.binaries.getFramework("MultiPlatformLibrary", org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG)
        }
    )
}
``` 

Then just launch task:

```shell script
./gradlew :sample:mpp-library:debugFatFramework
```

### Example 10 - plain file resource access
The first step is a create a resource file `test.txt` for example, in `commonMain/resources/MR/files`
After gradle sync we can get file by id `MR.files.test`
Moko-resources has out of box implementation function for read text files from common code - `readText()`

Usage on Android:
```
val text = MR.files.test.getText(context = this)
```
Usage on Apple:
```
val text = MR.files.test.readText()
```
If you want to read files not as text, add your own implementation to expect/actual FileResource

## Samples
Please see more examples in the [sample directory](sample).

Sample `mpp-hierarhical` contains usage of `org.jetbrains.kotlin.native.cocoapods` plugin and unit
tests with resources usage.
`Jvm-sample` to run it you should use IntelliJ IDEA.  
`macOS-sample` it contains two schemes. TestProj is the sample app and TestHierarchical is a splash-screen.  
`android-sample` TestHierarchical creates two launchers, the first one starts the sample-app at once, the second one allows to choose language before starting the sample.

## Set Up Locally
- The [resources directory](resources) contains the `resources` library;
- The [gradle-plugin directory](gradle-plugin) contains a gradle plugin with a `MR` class generator;
- The [sample directory](sample) contains sample apps for Android and iOS; plus the mpp-library connected to the apps.

## Contributing
All development (both new features and bug fixes) is performed in the `develop` branch. This way `master` always contains the sources of the most recently released version. Please send PRs with bug fixes to the `develop` branch. Documentation fixes in the markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` on release.

For more details on contributing please see the [contributing guide](CONTRIBUTING.md).

## License

    Copyright 2019 IceRock MAG Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat
[badge-windows]: http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat
[badge-mac]: http://img.shields.io/badge/platform-macos-111111.svg?style=flat
[badge-watchos]: http://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat
[badge-tvos]: http://img.shields.io/badge/platform-tvos-808080.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/platform-wasm-624FE8.svg?style=flat
[badge-nodejs]: https://img.shields.io/badge/platform-nodejs-68a063.svg?style=flat
[badge-iosx64]: https://img.shields.io/badge/platform-iosx64-CDCDCD?style=flat
[badge-iosarm64]: https://img.shields.io/badge/platform-iosarm64-CDCDCD?style=flat
[badge-iosSimulatorArm64]: https://img.shields.io/badge/platform-iosSimulatorArm64-CDCDCD?style=flat
[badge-macos64]: https://img.shields.io/badge/platform-macos64-111111?style=flat
