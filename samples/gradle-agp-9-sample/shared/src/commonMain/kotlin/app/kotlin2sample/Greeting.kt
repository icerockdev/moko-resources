package app.kotlin2sample

import app.gradle9sample.library.MR
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }

    fun getMR(): StringDesc {
        return MR.strings.hello_world.desc()
    }
}
