[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[ ![Download](https://api.bintray.com/packages/icerockdev/moko/moko-resources/images/download.svg) ](https://bintray.com/icerockdev/moko/moko-resources/_latestVersion)

# Базовые компоненты для мультиплатформы
## Resources
Мультиплатформенные ресурсы представляют собой `StringResource` + `PluralsResource`. 

`StringResource` и `PluralsResource` это классы идентификаторы ресурса на целевой платформе (на android
 это int из `R.string`, а на ios это строковый идентификатор). Они используются в специальном классе-объекте
 `MR` следующим образом:  
common sourceSet:
```kotlin
import com.icerockdev.mpp.core.resources.StringResource

expect object MR {
    @Suppress("ClassName")
    object string {
        val no_network_error: StringResource
        val unknown_error: StringResource
    }
}
```
android sourceSet:
```kotlin
import com.icerockdev.mpp.core.resources.StringResource

actual object MR {
    @Suppress("ClassName")
    actual object string {
        actual val no_network_error = StringResource(R.string.common_noNetworkError)
        actual val unknown_error = StringResource(R.string.common_unknownError)
    }
}
```
ios sourceSet:
```kotlin
import com.icerockdev.mpp.core.resources.StringResource

actual object MR {
    @Suppress("ClassName")
    actual object string {
        actual val no_network_error = StringResource("common.noNetworkError")
        actual val unknown_error = StringResource("common.unknownError")
    }
}
```
