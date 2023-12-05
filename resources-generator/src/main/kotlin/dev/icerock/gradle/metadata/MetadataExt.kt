package dev.icerock.gradle.metadata

fun MutableList<GeneratedObject>.addActual(actualObject: GeneratedObject){
    val expect: GeneratedObject? = firstOrNull {
        it.name == actualObject.name
                && it.generatorType == actualObject.generatorType
                && it.type == actualObject.type
    }

    if (expect != null){
        remove(expect)
    }

    add(actualObject)
}