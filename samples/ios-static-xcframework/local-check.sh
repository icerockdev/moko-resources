#
# Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

./gradlew clean && ./gradlew build assembleMultiPlatformLibraryXCFramework
log "ios-static-xcframework gradle build success"

(
cd ios-app &&
set -o pipefail &&
xcodebuild -scheme TestStaticXCFramework -project TestProj.xcodeproj -configuration Debug -sdk iphonesimulator -arch x86_64 build CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO | xcpretty
)
log "ios-static-xcframework ios xcode success"
