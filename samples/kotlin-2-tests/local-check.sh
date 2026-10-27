#
# Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

assert_apple_klib_resources() {
    local bundle_dir
    bundle_dir="$(find shared/build/classes/kotlin/iosArm64/main/klib -type d -name '*.bundle' -print -quit)"

    test -n "$bundle_dir"
    case "$(basename "$bundle_dir")" in
        *:*)
            echo "Apple bundle name is not portable: $bundle_dir" >&2
            exit 1
            ;;
    esac

    test -f "$bundle_dir/Contents/Info.plist"
    test -d "$bundle_dir/Contents/Resources/Assets.xcassets"
    test ! -f "$bundle_dir/Contents/Resources/Assets.car"
}

assert_linked_apple_resources() {
    local bundle_dir
    bundle_dir="$(find shared/build/bin/iosArm64/debugFramework -type d -name '*.bundle' -print -quit)"

    test -n "$bundle_dir"
    test -f "$bundle_dir/Contents/Info.plist"
    test -f "$bundle_dir/Contents/Resources/Assets.car"
    test ! -d "$bundle_dir/Contents/Resources/Assets.xcassets"
}

./gradlew clean assembleDebug
log "kotlin-2-tests mobile android success"

./gradlew :shared:compileKotlinIosArm64
assert_apple_klib_resources
log "kotlin-2-tests Apple KLib cross-compilation success"

if ! command -v xcodebuild &> /dev/null
then
    log "xcodebuild could not be found, skip ios checks"

    ./gradlew test lint
    log "kotlin-2-tests test success"

    ./gradlew assembleDebug assembleRelease jsJar jvmJar
    log "kotlin-2-tests build success"
else
    ./gradlew :shared:linkDebugFrameworkIosArm64
    assert_linked_apple_resources
    log "kotlin-2-tests Apple resources finalization success"

    ./gradlew build
    log "kotlin-2-tests success"
fi
