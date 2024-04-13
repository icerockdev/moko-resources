#
# Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

./gradlew clean assembleDebug
log "cm-resources-sample android success"

./gradlew clean jvmJar
log "cm-resources-sample jvm success"

./gradlew clean compileKotlinIosX64
log "cm-resources-sample ios success"

(
cd iosApp &&
set -o pipefail &&
xcodebuild -scheme iosApp -configuration Debug -sdk iphonesimulator -arch x86_64 build CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO | xcpretty
)
log "cm-resources-sample ios xcode success"
