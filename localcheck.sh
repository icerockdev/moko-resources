#
# Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

./gradlew -p resources-generator build publishToMavenLocal &&
./gradlew detekt &&
./gradlew clean && ./gradlew assembleDebug &&
./gradlew clean && ./gradlew jvmJar &&
./gradlew clean && ./gradlew compileKotlinIosX64 &&
./gradlew build publishToMavenLocal