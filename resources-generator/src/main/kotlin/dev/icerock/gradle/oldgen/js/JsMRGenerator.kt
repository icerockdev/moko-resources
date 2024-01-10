//    override fun processMRClass(mrClass: TypeSpec.Builder) {
//        val stringsLoaderInitializer = buildList {
//            val stringsObjectLoader = mrClass
//                .typeSpecs
//                .find { it.name == "strings" }
//                ?.propertySpecs
//                ?.find { it.name == "stringsLoader" }
//
//            val pluralsObjectLoader = mrClass
//                .typeSpecs
//                .find { it.name == "plurals" }
//                ?.propertySpecs
//                ?.find { it.name == "stringsLoader" }
//
//            if (stringsObjectLoader != null) {
//                add("strings.stringsLoader")
//            }
//            if (pluralsObjectLoader != null) {
//                add("plurals.stringsLoader")
//            }
//        }.takeIf(List<*>::isNotEmpty)
//            ?.joinToString(separator = " + ")
//
//        if (stringsLoaderInitializer != null) {
//            mrClass.addProperty(
//                PropertySpec.builder(
//                    "stringsLoader",
//                    ClassName("dev.icerock.moko.resources.provider", "RemoteJsStringLoader"),
//                ).initializer(stringsLoaderInitializer)
//                    .build()
//            )
//        }
//    }
