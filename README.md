[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://api.bintray.com/packages/icerockdev/moko/moko-resources/images/download.svg) ](https://bintray.com/icerockdev/moko/moko-resources/_latestVersion)

# Mobile Kotlin resources
This is a Kotlin MultiPlatform library that provide access to resources on iOS & Android with localization support based on system.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Samples](#samples)
- [Set Up Locally](#setup-locally)
- [Contributing](#contributing)
- [License](#license)

## Features
- **Strings, Plurals, Drawables** resources access from common code;
- **StringDesc** for lifecycle aware access to resources and unified localization getting on both platforms.

## Requirements
- Gradle version 5.4.1+
- Android API 21+
- iOS version 9.0+

## Installation
root build.gradle  
```groovy
allprojects {
    repositories {
        maven { url = "https://dl.bintray.com/icerockdev/moko" }
    }
}
```

project build.gradle
```groovy
dependencies {
    commonMainApi("dev.icerock.moko:resources:0.2.0")
}
```

settings.gradle  
```groovy
enableFeaturePreview("GRADLE_METADATA")
```

On iOS in addition to Kotlin library exist Pod - add in Podfile
```ruby
pod 'MultiPlatformLibraryResources', :git => 'https://github.com/icerockdev/moko-resources.git', :tag => 'release/0.2.0'
```
**MultiPlatformLibraryResources cocoapod requires that the framework compiled from kotlin be called 
MultiPlatformLibrary and be connected as cocoapod named MultiPlatformLibrary. Example [here](sample/ios-app/Podfile)**

## Usage
### Resources
`StringResource`, `PluralsResource`, `DrawableResource` is resource id containers. 
On Android & iOS resource id's is different types, so on both platform should be implemented initalization of resources. 

common sourceSet:
```kotlin
import dev.icerock.moko.resources.DrawableResource
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

expect object MR {
    object strings {
        val testString: StringResource
    }

    object plurals {
        val testPlural: PluralsResource
    }

    object drawables {
        val testDrawable: DrawableResource
    }
}
```
android sourceSet:
```kotlin
actual object MR {
    actual object strings {
        actual val testString = StringResource(R.string.test_string)
    }

    actual object plurals {
        actual val testPlural = PluralsResource(R.plurals.test_plural)
    }

    actual object drawables {
        actual val testDrawable = DrawableResource(R.drawable.test_drawable)
    }
}
```
**Also in android sourceset must exist android resources directory with used resources.**

ios sourceSet:
```kotlin
actual object MR {
    actual object strings {
        actual val testString = StringResource("test_string")
    }

    actual object plurals {
        actual val testPlural = PluralsResource("test_plural")
    }

    actual object drawables {
        actual val testDrawable = DrawableResource("test_image")
    }
}
```
**Resources with this keys must exist in application (Localizable.strings, Localizable.stringsdict, Assets).**

after that we can on platforms get keys of resources passed from common code:  
android:
```kotlin
fun test(string: StringResource, plural: PluralsResource, drawable: DrawableResource) {
    getString(string.resourceId)
    getQuantityString(plural.resourceId, 10)
    getDrawable(drawable.resourceId)
}
```
ios:
```swift
func test(string: StringResource, plural: PluralsResource, drawable: DrawableResource) {
    NSLocalizedString(string.resourceId)
    String.localizedStringWithFormat(NSLocalizedString(plural.resourceId), 10)
    UIImage(named: drawable.assetImageName)
}
```

### StringDesc
`StringDesc` can be used as dynamic source strings:
```kotlin
showError(StringDesc.Raw("here some error")) // pass raw string
showError(StringDesc.Resource(MR.strings.error)) // pass resource string which be localized by system
showError(StringDesc.ResourceFormatted(MR.strings.error_format, "string arg", 10)) // pass resource string which be localized by system, and applied formatting (String.format style)
showError(StringDesc.Plural(MR.plurals.days, 10)) // pass plural string which be localized & get correct on current language plural variant for number
showError(StringDesc.PluralFormatted(MR.plurals.days, 10, 10)) // pass plural string which be localized & get correct on current language plural variant for number, in result string will be passed format args (String.format style)
showError(StringDesc.Raw("here some error") + StringDesc.Resource(MR.strings.error)) // pass composition (just merged results of stringdesc)
```
android reading of StringDesc:
```kotlin
fun showError(error: StringDesc) {
    val string = error.getString(context = this) // in this call passed resources ids will be used for get correct string
    println(string)
}
```
iOS reading of StringDesc:
```swift
func showError(error: StringDesc) {
    let string = error.localized() // in this call passed resources ids will be used for get correct string
    print(string)
}
```

## Samples
More examples can be found in the [sample directory](sample).

## Set Up Locally 
- In [resources directory](resources) contains `resources` library;
- In [sample directory](sample) contains samples on android, ios & mpp-library connected to apps;
- For test changes locally use `:resources:publishToMavenLocal` gradle task, after it samples will use locally published version.

## Contributing
All development (both new features and bug fixes) is performed in `develop` branch. This way `master` sources always contain sources of the most recently released version. Please send PRs with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` during release.

More detailed guide for contributers see in [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2019 IceRock MAG Inc
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.