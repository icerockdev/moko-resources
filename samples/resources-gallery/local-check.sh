#
# Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

./gradlew clean assembleDebug
log "resources-gallery android success"

./gradlew clean jvmJar
log "resources-gallery jvm success"

./gradlew clean jsBrowserDistribution
log "resources-gallery js success"

if ! command -v xcodebuild &> /dev/null
then
    log "xcodebuild could not be found, skip ios checks"

    ./gradlew build
    log "resources-gallery full build success"
else
    ./gradlew clean compileKotlinIosX64
    log "resources-gallery ios success"

    # rerun tasks because kotlinjs compilation broken with build cache :(
    ./gradlew clean podspec build generateDummyFramework --rerun-tasks
    log "resources-gallery clean build success"

    (
    cd ios-app &&
    pod install &&
    set -o pipefail &&
    xcodebuild -scheme TestProj -workspace TestProj.xcworkspace -configuration Debug -sdk iphonesimulator -arch x86_64 build CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO | xcpretty
    )
    log "resources-gallery ios xcode success"

    (
    cd macos-app &&
    pod install &&
    set -o pipefail &&
    xcodebuild -scheme TestProj -workspace macos-app.xcworkspace -configuration Debug -sdk macosx -arch x86_64 build CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO | xcpretty
    )
    log "resources-gallery macos xcode success"
fi
