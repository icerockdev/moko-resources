![moko-resources](img/logo.png)  
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
buildscript {
    repositories {
        maven { url = "https://dl.bintray.com/icerockdev/plugins" }
    }

    dependencies {
        classpath "dev.icerock.moko:resources-generator:0.3.0"
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
    commonMainApi("dev.icerock.moko:resources:0.3.0")
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
**MultiPlatformLibraryResources cocoapod requires that the framework compiled from kotlin be named 
MultiPlatformLibrary and be connected as cocoapod named MultiPlatformLibrary. 
Example [here](sample/ios-app/Podfile).
To simplify configuration with MultiPlatformFramework you can use [mobile-multiplatform-plugin](https://github.com/icerockdev/mobile-multiplatform-gradle-plugin)**
`MultiPlatformLibraryResources` cocoapod contains extension `localized` for `StringDesc`.

## Usage
### Example 1 - use simple localization string
First step - create in `commonMain/resources/MR/base` file `strings.xml` with content:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string">My default localization string</string>
</resources>
```
Next - create in `commonMain/resource/MR/<languageCode>` file `strings.xml` with localized strings. For example we create `commonMain/resource/MR/ru` for russian localization:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string">Моя строка локализации по умолчанию</string>
</resources>
```
After add of resources we can call gradle sync or execute gradle task `generateMRcommonMain` - this action
 will generate `MR` class contains `MR.strings.my_string` which we can use in `commonMain`:
```kotlin
fun getMyString(): StringDesc {
  return StringDesc.Resource(MR.strings.my_string)
}
``` 
After it we can use our functions on platform side:  
android:
```kotlin
val string = getMyString().toString(context = this)
```
ios:
```swift
let string = getMyString().localized()
```
Note: `StringDesc` is multiple source container for Strings - in StringDesc may be used resource, or plural, or formatted variants, or raw string. To convert `StringDesc` in `String` on android must be called `toString(context)` (context needed for resources usage), on ios - `localized()`. 

### Example 2 - use formatted localization string
Add in `commonMain/resources/MR/base/strings.xml`:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
    <string name="my_string_formatted">My format '%s'</string>
</resources>
```
After it add in other languages localized values like in example 1.
Next create in `commonMain` function:
```kotlin
fun getMyFormatDesc(input: String): StringDesc {
  return StringDesc.ResourceFormatted(MR.strings.my_string_formatted, input)
}
```
And just like in example 1 usage on platform side:
android:
```kotlin
val string = getMyFormatDesc("hello").toString(context = this)
```
ios:
```swift
let string = getMyFormatDesc(input: "hello").localized()
```

### Example 3 - use plural string
First step - create in `commonMain/resources/MR/base` file `plurals.xml` with content:
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
After it add in other languages localized values like in example 1.
Next create in `commonMain` function:
```kotlin
fun getMyPluralDesc(quantity: Int): StringDesc {
  return StringDesc.Plural(MR.plurals.my_plural, quantity)
}
```
And just like in example 1 usage on platform side:
android:
```kotlin
val string = getMyPluralDesc(10).toString(context = this)
```
ios:
```swift
let string = getMyPluralDesc(quantity: 10).localized()
```

### Example 4 - use plural formatted string
First step - create in `commonMain/resources/MR/base` file `plurals.xml` with content:
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
After it add in other languages localized values like in example 1.
Next create in `commonMain` function:
```kotlin
fun getMyPluralFormattedDesc(quantity: Int): StringDesc {
  // we pass quantity as selector for correct plural string and for pass quantity as argument for formatting
  return StringDesc.PluralFormatted(MR.plurals.my_plural, quantity, quantity)  
}
```
And just like in example 1 usage on platform side:
android:
```kotlin
val string = getMyPluralFormattedDesc(10).toString(context = this)
```
ios:
```swift
let string = getMyPluralFormattedDesc(quantity: 10).localized()
```

### Example 5 - pass raw string or resource
If we already use some resource for placeholder value we can use `StringDesc` for simple change of string source:
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
android:
```kotlin
val string1 = getUserName(user).toString(context = this) // we got name from User model
val string2 = getUserName(null).toString(context = this) // we got name_placeholder from resources
```
ios:
```swift
let string1 = getUserName(user: user).localized() // we got name from User model
let string2 = getUserName(user: null).localized() // we got name_placeholder from resources
```

## Samples
More examples can be found in the [sample directory](sample).

## Set Up Locally 
- In [resources directory](resources) contains `resources` library;
- In [gradle-plugin directory](gradle-plugin) contains gradle plugin with `MR` class generator;
- In [sample directory](sample) contains samples on android, ios & mpp-library connected to apps;
- For test changes locally use `:resources:publishToMavenLocal` gradle task, after it samples will use locally published version.
- For test changes of plugin locally use `:gradle-plugin:publishToMavenLocal` gradle task, after it samples will use locally published version.

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