
//    protected open val sourcesGenerationDir: File = settings.sourceSetDir.asFile
//    protected open val resourcesGenerationDir: File = settings.resourcesDir.asFile
//    protected open val assetsGenerationDir: File = settings.assetsDir.asFile
//
//    internal fun generate() {
//        sourcesGenerationDir.deleteRecursively()
//        resourcesGenerationDir.deleteRecursively()
//        assetsGenerationDir.deleteRecursively()
//
//        beforeMRGeneration()
//
//        val file = generateFileSpec()
//        file?.writeTo(sourcesGenerationDir)
//
//        afterMRGeneration()
//    }
