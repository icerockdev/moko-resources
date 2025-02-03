#
# Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

./gradlew clean assembleDebug
log "kotlin-2-dynamic-sample mobile android success"

if ! command -v xcodebuild &> /dev/null
then
    log "xcodebuild could not be found, skip ios checks"

    ./gradlew build
    log "kotlin-2-dynamic-sample full build success"
else
    ./gradlew clean compileKotlinIosX64
    log "kotlin-2-dynamic-sample mobile ios success"

    (
    cd iosApp &&
    set -o pipefail &&
    xcodebuild -scheme dev -configuration Debug -destination "generic/platform=iOS Simulator" build CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO | xcpretty
    )
    log "kotlin-2-dynamic-sample mobile ios xcode success"
fi
