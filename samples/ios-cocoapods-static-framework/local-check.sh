#
# Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

./gradlew clean compileKotlinIosX64
log "ios-cocoapods-static-framework ios success"

./gradlew clean podspec build generateDummyFramework
log "ios-cocoapods-static-framework full build success"

(
cd iosApp &&
pod install &&
set -o pipefail &&
xcodebuild -scheme iosApp -workspace iosApp.xcworkspace -configuration Debug -sdk iphonesimulator -arch x86_64 build CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO
)

log "ios-cocoapods-static-framework ios xcode success"
