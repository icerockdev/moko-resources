package dev.icerock.gradle.metadata

fun MutableList<GeneratedObject>.addActual(actualObject: GeneratedObject){
    val expect: GeneratedObject? = firstOrNull {
        it.name == actualObject.name
                && it.generatorType == actualObject.generatorType
                && it.type == actualObject.type
    }

    if (expect != null) {
        remove(expect)
    }

    add(actualObject)
}

fun List<GeneratedObject>.getExpectInterfaces(): List<GeneratedObject> {
    return filter { it.isExpectInterface }
}

fun List<GeneratedObject>.getActualInterfaces(generatorType: GeneratorType): List<GeneratedObject> {
    return filter {
        it.isActualInterface && it.generatorType == generatorType
    }
}

fun List<GeneratedObject>.objectsWithProperties(
    targetObject: GeneratedObject,
): List<GeneratedObject> {
    val objectsWithProperties = mutableListOf<GeneratedObject>()

    this.forEach { genObject ->
        if (genObject.generatorType == targetObject.generatorType && genObject.properties.isNotEmpty()) {
            objectsWithProperties.add(genObject)
        }

        genObject.objects.forEach { innerObject ->
            if (innerObject.generatorType == targetObject.generatorType && innerObject.properties.isNotEmpty()) {
                objectsWithProperties.add(innerObject)
            }
        }
    }

    return objectsWithProperties
}

