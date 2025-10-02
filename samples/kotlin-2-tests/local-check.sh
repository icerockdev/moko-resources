#
# Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

./gradlew clean assembleDebug
log "kotlin-2-tests mobile android success"

if ! command -v xcodebuild &> /dev/null
then
    log "xcodebuild could not be found, skip ios checks"

    ./gradlew test lint
    log "kotlin-2-tests test success"

    ./gradlew assembleDebug assembleRelease jsJar jvmJar
    log "kotlin-2-tests build success"
else
    ./gradlew build
    log "kotlin-2-tests success"
fi
