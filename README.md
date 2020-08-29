![moko-resources](img/logo.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://api.bintray.com/packages/icerockdev/moko/moko-resources/images/download.svg) ](https://bintray.com/icerockdev/moko/moko-resources/_latestVersion) ![kotlin-version](https://img.shields.io/badge/kotlin-1.4.0-orange)

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
- **StringDesc** for lifecycle-aware access to resources and unified localization on both platforms;
- **FatFrameworkWithResourcesTask** Gradle task.

## Requirements
- Gradle version 6.0+
- Android API 16+
- iOS version 9.0+

## Versions
- kotlin 1.3.50
  - 0.1.0
  - 0.2.0
  - 0.3.0
  - 0.4.0
- kotlin 1.3.60
  - 0.5.0
- kotlin 1.3.61
  - 0.6.0
  - 0.6.1
  - 0.6.2
  - 0.7.0
  - 0.8.0
- kotlin 1.3.70
  - 0.9.0
- kotlin 1.3.71
  - 0.9.1
- kotlin 1.3.72
  - 0.10.0
  - 0.10.1
  - 0.11.0
  - 0.11.1
- kotlin 1.4.0
  - 0.12.0

## Installation
root build.gradle  
```groovy
buildscript {
    repositories {
        maven { url = "https://dl.bintray.com/icerockdev/plugins" }
    }

    dependencies {
        classpath "dev.icerock.moko:resources-generator:0.12.0"
    }
}


allprojects {
    repositories {
        maven { url = "https://dl.bintray.com/icerockdev/moko" }
    }
}
```

project build.gradle
```groovy
apply plugin: "dev.icerock.mobile.multiplatform-resources"

dependencies {
    commonMainApi("dev.icerock.moko:resources:0.12.0")
}

multiplatformResources {
    multiplatformResourcesPackage = "org.example.library" // required
    iosBaseLocalizationRegion = "en" // optional, default "en"
    multiplatformResourcesSourceSet = "commonClientMain"  // optional, default "commonMain"
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

### Example 8 - pass font
Fonts resources directory is `commonMain/resources/MR/fonts`.  
Font name should be this pattern: `<fontFamily>-<fontStyle>` like:
- `Raleway-Bold.ttf`
- `Raleway-Regular.ttf`
- `Raleway-Italic.ttf`
Supported only `ttf` resources for now.

If we add to `commonMain/resources/MR/fonts` files:
- `Raleway-Bold.ttf`
- `Raleway-Regular.ttf`
- `Raleway-Italic.ttf`

We got autogenerated `MR.fonts.Raleway.italic`, `MR.fonts.Raleway.regular`, `MR.fonts.Raleway.bold` `FontResource` in code, that we can use:
- Android: `textView.typeface = font.getTypeface(context = this)`
- iOS: `textView.font = font.uiFont(withSize: 14.0)`

### Gradle task for creating Fat Framework with resources 

If you want to create Fat Framework for iOS with all resources from KMP Gradle module you should use
extended Gradle task `FatFrameworkWithResourcesTask`. There is example of 
`FatFrameworkWithResourcesTask` task using for the `mpp-library` module of the Sample. In the end
of the `sample/mpp-library/build.gradle.kts` file: 

```kotlin
kotlin {
    tasks.register("debugFatFramework", dev.icerock.gradle.generator.FatFrameworkWithResourcesTask::class) {
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
}
``` 

Then just launch task:

```shell script
./gradlew :sample:mpp-library:debugFatFramework
```

## Samples
Please see more examples in the [sample directory](sample).

## Set Up Locally 
- The [resources directory](resources) contains the `resources` library;
- The [gradle-plugin directory](gradle-plugin) contains a gradle plugin with a `MR` class generator;
- The [sample directory](sample) contains sample apps for Android and iOS; plus the mpp-library connected to the apps;
- For local testing use the `./publishToMavenLocal.sh` script - so that sample apps use the locally published version.

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
