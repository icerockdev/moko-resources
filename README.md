![moko-resources](img/logo.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/resources) ](https://repo1.maven.org/maven2/dev/icerock/moko/resources) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=resources)
![badge][badge-android]
![badge][badge-iosX64]
![badge][badge-iosArm64]
![badge][badge-iosSimulatorArm64]
![badge][badge-macosArm64]
![badge][badge-macosX64]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-wasmjs]

# Mobile Kotlin resources

This is a Kotlin MultiPlatform library (and Gradle plugin) that provides access to the resources on
macOS, iOS, Android the JVM and JS/Browser with the support of the default system localization.

Also MOKO resources
supports [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) so you can
implement all your UI in Kotlin with Jetpack Compose and MOKO resources.

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

- **Strings, Plurals** to access the corresponding resources from common code;
- **Colors** with light/dark mode support;
- **Compose Multiplatform** support;
- **Images** support (`svg`, `png`, `jpg`) with light/dark mode support;
- **Fonts** support (`ttf`, `otf`);
- **Files** support (as `raw` or `assets` for android);
- **StringDesc** for lifecycle-aware access to resources and unified localization on both platforms;
- **Static** iOS frameworks support;
- **Fat and XC** frameworks support.

## Requirements

- Gradle version 7.5+
- Kotlin 1.9.20+
- Android Gradle Plugin 7.4.2+
- Android API 16+
- iOS version 11.0+
- Compose Multiplatform 1.6.0+

## Installation

### Gradle setup

root build.gradle

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath "dev.icerock.moko:resources-generator:0.25.0"
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
    commonMainApi("dev.icerock.moko:resources:0.25.0")
    commonMainApi("dev.icerock.moko:resources-compose:0.25.0") // for compose multiplatform

    commonTestImplementation("dev.icerock.moko:resources-test:0.25.0")
}

multiplatformResources {
    resourcesPackage.set("org.example.library") // required
    resourcesClassName.set("SharedRes") // optional, default MR
    resourcesVisibility.set(MRVisibility.Internal) // optional, default Public
    iosBaseLocalizationRegion.set("en") // optional, default "en"
    iosMinimalDeploymentTarget.set("11.0") // optional, default "9.0"
}
```

#### Custom resource sourceSet

If you need custom path for source of resources, you need add in plugin configuration resourcesSourceSets option:
project build.gradle

```groovy
multiplatformResources {
    resourcesPackage.set("org.example.library.customResource") // required
    resourcesSourceSets {
        getByName("jvmMain").srcDirs(
            File(projectDir, "customResources")
        )
    }  
}
```

On next step, you must create inside of project directory folder with name: `customResources`, and moved your resources there. 

```
- projectDirectory
-- customResources
--- assets
--- base
--- image
```

Example of custom sourceSet in: `resources-gallery` sample, inside `jvm-app`

#### Export classes to Swift

To use `toUIColor()`, `toUIImage()`, `desc()` and other iOS extensions from Swift - you
should [add `export` declarations](https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#export-dependencies-to-binaries):

```
framework {
    export("dev.icerock.moko:resources:0.25.0")
    export("dev.icerock.moko:graphics:0.9.0") // toUIColor here
}
```

#### Multi-module Gradle projects

If you have multiple gradle modules and resources stored not in module that compiles into framework for iOS, for example:
```
- shared
-- resources
-- feature-1
-- feature-2
```
You should enable moko-resources gradle plugin in `resources` module, that contains resources, AND in `shared` module, that compiles into framework for iOS (same for jvm, JS, macos targets. Only android will works without this).

### Xcode setup

For correct work of plugin tasks you need disable `ENABLE_USER_SCRIPT_SANDBOXING` in .xcodeproj file:
Xcode > Build Settings > Build Options > User Script Sandbox set `NO` 

In iOS/macOS Info.plist need to add localizations, to use localizations strings.

```xml

<key>CFBundleLocalizations</key><array>
<string>en</string>
<string>ru</string>
</array>
```

in array should be added all used languages.

### Android build types

If your project includes a build type, for example `staging` which isn't in moko-resources. That
isn't an issue. Use matchingFallbacks to specify alternative matches for a given build type, as
shown below

```
buildTypes {
    staging {
        initWith debug
        matchingFallbacks = ['debug']
    }
}
```

### JS Webpack

JS/Browser generates json files which is included in webpack by default.
For more details about JS see `samples/resources-gallery/web-app` sample

### iOS/macOS static kotlin frameworks support

Static framework can't have own resources, so we should setup additional `Build Phase` in Xcode
that will copy resources to application. 

> **⚠ Warning**  
> 
> This phase should be placed after Kotlin Framework Compilation phase.

Please replace `:yourframeworkproject` to kotlin project gradle path, and set correct relative
path (`$SRCROOT/../` in example).

#### With org.jetbrains.kotlin.native.cocoapods

In Xcode add `Build Phase` (at end of list) with script:

```shell script
"$SRCROOT/../gradlew" -p "$SRCROOT/../" :yourframeworkproject:copy`YourFrameworkName`FrameworkResourcesToApp \
    -Pmoko.resources.BUILT_PRODUCTS_DIR="$BUILT_PRODUCTS_DIR" \
    -Pmoko.resources.CONTENTS_FOLDER_PATH="$CONTENTS_FOLDER_PATH" \
    -Pkotlin.native.cocoapods.platform="$PLATFORM_NAME" \
    -Pkotlin.native.cocoapods.archs="$ARCHS" \
    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION" 
```

`YourFrameworkName` is name of your project framework. Please, see on a static framework warning for get correct task name.

#### Without org.jetbrains.kotlin.native.cocoapods

In Xcode add `Build Phase` (at end of list) with script:

```shell script
"$SRCROOT/../gradlew" -p "$SRCROOT/../" :yourframeworkproject:copyFrameworkResourcesToApp \
    -Pmoko.resources.PLATFORM_NAME="$PLATFORM_NAME" \
    -Pmoko.resources.CONFIGURATION="$CONFIGURATION" \
    -Pmoko.resources.ARCHS="$ARCHS" \
    -Pmoko.resources.BUILT_PRODUCTS_DIR="$BUILT_PRODUCTS_DIR" \
    -Pmoko.resources.CONTENTS_FOLDER_PATH="$CONTENTS_FOLDER_PATH" 
```

#### Disable warning about static framework usage

To disable warnings about static framework in gradle.properties:

```xml
moko.resources.disableStaticFrameworkWarning=true
```

### iOS executable

When you use `executable` kotlin target you should add custom build phase to xcode, after kotlin
compilation:

```shell
"$SRCROOT/../gradlew" -p "$SRCROOT/../" :shared:copyResourcesDebugExecutableIosSimulatorArm64 \
    -Pmoko.resources.BUILT_PRODUCTS_DIR=$BUILT_PRODUCTS_DIR \
    -Pmoko.resources.CONTENTS_FOLDER_PATH=$CONTENTS_FOLDER_PATH
```

`copyResourcesDebugExecutableIosSimulatorArm64` should be configured depends on target.

Configured sample you can see in `samples/kotlin-ios-app`

### Creating Fat Framework with resources

Just
use `FatFrameworkTask` [from kotlin plugin](https://kotlinlang.org/docs/mpp-build-native-binaries.html#build-universal-frameworks)
.

### Creating XCFramework with resources

Just
use `XCFramework` [from kotlin plugin](https://kotlinlang.org/docs/mpp-build-native-binaries.html#build-xcframeworks)
.

But if you use **static frameworks** required additional setup - add to Xcode build phase (at end):

```bash
"$SRCROOT/../gradlew" -p "$SRCROOT/../" :shared:copyResourcesMultiPlatformLibraryReleaseXCFrameworkToApp \
    -Pmoko.resources.BUILT_PRODUCTS_DIR=$BUILT_PRODUCTS_DIR \
    -Pmoko.resources.CONTENTS_FOLDER_PATH=$CONTENTS_FOLDER_PATH
```

and add in your build.gradle config:
```kotlin
multiplatformResources {
    configureCopyXCFrameworkResources("MultiPlatformLibrary")
}
```

replace "MultiPlatformLibrary" with name that you use in `XCFramework` creation.

Details you can check in sample `samples/ios-static-xcframework`.

## Usage

### Example 1 - simple localization string

The first step is a create a file `strings.xml` in `commonMain/moko-resources/base` with the following
content:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string">My default localization string</string>
</resources>
```

Next - create a file `strings.xml` with localized strings
in `commonMain/moko-resources/<languageCode>`. Here's an example of
creating `commonMain/moko-resources/ru` for a Russian localization:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string">Моя строка локализации по умолчанию</string>
</resources>
```

After adding the resources we can call a gradle sync or execute a gradle
task `generateMRcommonMain`. This will generate a `MR` class containing `MR.strings.my_string`,
which we can use in `commonMain`:

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

JS:

```kotlin
val strings = MR.stringsLoader.getOrLoad() // loading localization from a remote file
val string = getMyString().localized(strings)
```

Note: `StringDesc` is a multiple-source container for Strings: in StringDesc we can use a resource,
plurals, formatted variants, or raw string. To convert `StringDesc` to `String` on Android
call `toString(context)` (a context is required for the resources usage), on iOS -
call `localized()`.

#### Compose Multiplatform

with compose you can just call in `commonMain`

```kotlin
val string: String = stringResource(MR.strings.my_string)
```

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

iOS SwiftUI:

```swift
let resource = MR.strings().email
Text(
    LocalizedStringKey(resource.resourceId),
    bundle: resource.bundle
)
```

Note: more info in issue [#126](https://github.com/icerockdev/moko-resources/issues/126).

### Example 2 - formatted localization string

In `commonMain/moko-resources/base/strings.xml` add:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string_formatted">My format \'%s\'</string>
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

Warning: Do no mix positioned placeholders with unpositioned ones within a string, as this may lead
to
different behaviour on different platforms. Stick to one style for each string.

### Example 3 - plural string

The first step is to create a file `plurals.xml` in `commonMain/moko-resources/base` with the
following content:

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

The first step is to create file `plurals.xml` in `commonMain/moko-resources/base` with the following
content:

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

Compose:

With compose, you can simply use `pluralStringResource`

```kotlin
Text(
    text = pluralStringResource(
        MR.plurals.runtime_format,
        quantity,
        quantity
    )
)
```

### Example 5 - pass raw string or resource

If we already use some resources as a placeholder value, we can use `StringDesc` to change the
string source:

```kotlin
fun getUserName(user: User?): StringDesc {
    if (user != null) {
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
StringDesc.localeType = StringDesc.LocaleType.System
```

Android:

Add this to your app's `build.gradle` to keep all locales in resulting [App Bundle](https://www.youtube.com/watch?v=IPLhLu0kvYw&ab_channel=AndroidDevelopers) if you want them all to be available in runtime (Otherwise, when the user downloads the app from PlayMarket, resources for his system locale only will be available).

```
android {
    bundle {
        language {
            enableSplit = false
        }
    }
}
```

### Example 7 - Shared Images

Place images in the `commonMain/moko-resources/images` directory. Nested directories are also supported.

#### png and jpg

Image names should end with one of:

- `@0.75x` - android ldpi;
- `@1x` - android mdpi, ios 1x;
- `@1.5x` - android hdpi;
- `@2x` - android xhdpi, ios 2x;
- `@3x` - android xxhdpi, ios 3x;
- `@4x` - android xxxhdpi.

If we add the following files to `commonMain/moko-resources/images`:

- `home_black_18@1x.png`
- `home_black_18@2x.png`

Then we get an autogenerated `MR.images.home_black_18` `ImageResource` in code. Usage:

- Android: `imageView.setImageResource(image.drawableResId)`
- iOS: `imageView.image = image.toUIImage()`

#### dark mode

To support Dark Mode images, you can add -dark and optionally -light to the name of an image. Make sure the rest of the name matches the corresponding light mode image:

- `car.svg`
- `car-dark.svg`

#### svg

The Image generator also supports `svg` files.

If we add the following file to `commonMain/moko-resources/images`:

- `car_black.svg`

Then we get an autogenerated `MR.images.car_black` `ImageResource` in code. Usage:

- Android: `imageView.setImageResource(image.drawableResId)`
- iOS: `imageView.image = image.toUIImage()`

On Android it is a `VectorDrawable`,

On iOS iOS 13 or later it is a `UIImage` in the Assets catalog with `preserves-vector-representation` set to `true`.

#### images by name

You can get images by their name, too.

In `commonMain` create a `Resources.kt` file with the content below.


```kotlin
fun getImageByFileName(name: String): ImageResource {
    val fallbackImage = MR.images.transparent
    return MR.images.getImageByFileName(name) ?: fallbackImage
}
```

Usage: 

- Android: `imageView.setImageResource(getImageByFileName("image_name"))`
- iOS: `imageView.image = ResourcesKt.getImageByFileName(name: "image_name").toUIImage()!`

#### Compose Multiplatform

With compose, you can simply use a `painterResource` in `commonMain`

```kotlin
val painter: Painter = painterResource(MR.images.home_black_18)
```

#### SwiftUI

For SwiftUI, create this `Image` extension:

```swift
extension Image {
    init(resource: KeyPath<MR.images, ImageResource>) {
        self.init(uiImage: MR.images()[keyPath: resource].toUIImage()!)
    }
}
```

Then, you can refer to `ImageResource`s directly by their key path, which provides compiler errors for typos or missing resources:

```swift
Image(resource: \.home_black_18)
```

### Example 8 - pass font

Fonts resources directory is `commonMain/moko-resources/fonts`.
Supported type of resources:
- `ttf`
- `otf`

If we add to `commonMain/moko-resources/fonts` files:
- `Raleway-Bold.ttf`
- `Raleway-Regular.otf`
- `Raleway-Italic.ttf`

We got autogenerated:
- `MR.fonts.raleway_italic`,
- `MR.fonts.raleway_regular`,
- `MR.fonts.raleway_bold_italic`

in code, that we can use:

- Android: `textView.typeface = font.getTypeface(context = this)`
- iOS: `textView.font = font.uiFont(withSize: 14.0)`

#### Compose Multiplatform

with compose you can just call in `commonMain`

```kotlin
val fontFamily: FontFamily = fontFamilyResource(MR.fonts.Raleway.italic)
```

or you can get `Font`

```kotlin
val font: Font = MR.fonts.Raleway.italic.asFont(
  weight = FontWeight.Normal, // optional
  style = FontStyle.Normal // optional
)
```
#### SwiftUI

For SwiftUI, create this `Font` extension:

```swift
extension Font {
    init(resource: KeyPath<MR.fonts, FontResource>, withSize: Double = 14.0) {
        self.init(MR.fonts()[keyPath: resource].uiFont(withSize: withSize))
    }
}
```

Then, you can refer to `FontResource`s directly by their key path, which provides compiler errors for typos or missing resources:

```swift
 Text("Text displayed resource font")
   .font(Font(resource: \.raleway_regular, withSize: 14.0))
```

### Example 9 - pass colors

Colors resources directory is `commonMain/moko-resources/colors`.  
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
`ColorResource` can be read from platform side:

android:

```kotlin
val color: Int = MR.colors.valueColor.getColor(context = this)
```

iOS:

```swift
val color: UIColor = MR.colors.valueColor.getUIColor()
```

macOS:

```swift
val color: NSColor = MR.colors.valueColor.getNSColor()
```

jvm:

```kotlin
val light: Color = MR.colors.valueColor.lightColor
val dark: Color = MR.colors.valueColor.darkColor
```

web:

```kotlin
val light: Color = MR.colors.valueColor.lightColor
val dark: Color = MR.colors.valueColor.darkColor
```

#### Compose Multiplatform

with compose you can just call in `commonMain`

```kotlin
val color: Color = colorResource(MR.colors.valueColor)
```

### Example 10 - plain file resource access

The first step is a create a resource file `test.txt` for example,
in `commonMain/moko-resources/files`
After gradle sync we can get file by id `MR.files.test`
Moko-resources has out of box implementation function for read text files from common
code - `readText()`

Usage on Android:

```
val text = MR.files.test.getText(context = this)
```

Usage on Apple:

```
val text = MR.files.test.readText()
```

If you want to read files not as text, add your own implementation to expect/actual FileResource

#### Compose Multiplatform

with compose you can just call in `commonMain`

```kotlin
val fileContent: String? by MR.files.test.readTextAsState()
```

### Example 11 - assets access

Assets allow you save directories hierarchy (in files structure is plain). Locate files
to `commonMain/moko-resources/assets` and access to it by `MR.assets.*`

#### Compose Multiplatform

with compose you can just call in `commonMain`

```kotlin
val assetContent: String? by MR.assets.test.readTextAsState()
```

## Known issues

### iOS shows key instead of localized text

1. check that generated `Localizable.strings` file is valid - open it by Xcode (located in `shared/shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/<project-name>:shared.bundle/Contents/Resources/Base.lproj/Localizable.strings` and in other `.lproj` directories. If Xcode show error in file - you should fix content of strings.xml (for example you use some special character that broke file).

2. check that your generated `.bundle` exist inside application at runtime. In Xcode inside group `Products` select your application and click `Show in Finder`. Then click `Show Package Contents`. Inside `.app` you should see `.bundle` in root directory if you use static framework. And in `Frameworks/shared.framework` if you use dynamic framework. If `bundle` missed - check installation guide. Specifically xcode build phase part if you use static framework. And check that you apply moko-resources plugin in `shared` gradle module.

3. check that your `strings.xml` contains all keys for language that you use. If you have keys `test1`, `test2` in `Base/strings.xml`, and only `test1` in `ru/strings.xml` then you got key instead of text in ru locale for `test2` key. iOS not fallback to base locale now

## Samples

In `samples` directory you can find multiple projects showed different usages.

- [resources-gallery](samples/resources-gallery) - android, iOS, macOS, jvm, web applications with
  all resources types usage.
- [compose-resources-gallery](samples/compose-resources-gallery) - android, iOS, jvm, web
  Compose Multiplatform applications with all resources types usage.
- [android-mpp-app](samples/android-mpp-app) - android application that configured as multiplatform
  module.
- [auto-manifest](samples/auto-manifest) - sample of
  usage [auto-manifest](https://github.com/GradleUp/auto-manifest) plugin.
- [compose-jvm-app](samples/compose-jvm-app) - Compose Desktop jvm application with resources usage.
- [ios-static-xcframework](samples/ios-static-xcframework) - iOS application with static XCFramework
  and resources.
- [kotlin-ios-app](samples/kotlin-ios-app) - iOS application without swift - full code on kotlin,
  with resources usage.

## Set Up Locally

In root of repository contains `moko-resources` gradle project - libraries and gradle plugin. You
can just open project in IDE and develop. Then for tests in samples you should run
run `./gradlew publishToMavenLocal` gradle task. After this you can open any sample from `samples`
in IDE and test your local version of `moko-resources`.

To check your changes before pull request run:
```bash
# check lib & plugin
./local-check.sh
# check samples
./local-samples-check.sh
```

## Contributing

All development (both new features and bug fixes) is performed in the `develop` branch. This
way `master` always contains the sources of the most recently released version. Please send PRs with
bug fixes to the `develop` branch. Documentation fixes in the markdown files are an exception to
this rule. They are updated directly in `master`.

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

[badge-iosX64]: https://img.shields.io/badge/platform-iosX64-CDCDCD?style=flat

[badge-iosArm64]: https://img.shields.io/badge/platform-iosArm64-CDCDCD?style=flat

[badge-iosSimulatorArm64]: https://img.shields.io/badge/platform-iosSimulatorArm64-CDCDCD?style=flat

[badge-macosX64]: https://img.shields.io/badge/platform-macosX64-111111?style=flat

[badge-macosArm64]: https://img.shields.io/badge/platform-macosArm64-111111?style=flat    
