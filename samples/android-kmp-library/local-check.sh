#
# Copyright 2026 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

log() {
  echo "\033[0;32m> $1\033[0m"
}

assert_generated_resources() {
    local sample_dir="core/build/generated/moko-resources"
    local android_sources="$sample_dir/androidMain/src"
    local jvm_sources="$sample_dir/jvmMain/src"
    local jvm_properties
    local unexpected_properties

    test -f "$sample_dir/androidMain/res/values/multiplatform_strings.xml"

    unexpected_properties="$(
        find "$sample_dir/androidMain/res" -type f -name '*.properties' -print -quit
    )"
    if test -n "$unexpected_properties"
    then
        echo "JVM resource generated in Android res directory: $unexpected_properties" >&2
        exit 1
    fi

    jvm_properties="$(
        find "$sample_dir/jvmMain/res" -type f -name '*.properties' -print -quit
    )"
    test -n "$jvm_properties"
    grep -R -q 'R.string.hello' "$android_sources"
    grep -R -q 'ClassLoader' "$jvm_sources"
}

../../gradlew -p . clean :core:assemble
assert_generated_resources
log "Android KMP library resources success"
