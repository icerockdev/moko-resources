# Compatibility policy

Consumer minimums and the `resources-generator` compile toolchain are versioned independently.
Updating a generator build dependency does not implicitly raise a consumer requirement.

## Version matrix

| Scope                | Version | Contract                                                                                   |
|----------------------|---------|--------------------------------------------------------------------------------------------|
| Consumer Kotlin      | 2.1.0   | Minimum for published artifacts; also used as the generator language and API level.        |
| Generator Kotlin     | 2.4.20  | Build toolchain only; does not change the consumer minimum.                                |
| Generator KotlinPoet | 2.3.0   | Internal implementation dependency; absent from generated and public APIs.                 |
| Consumer AGP         | 8.3.0   | Oldest supported AGP runtime.                                                              |
| Generator AGP API    | 8.13.2  | Last AGP 8 compile API, used via `compileOnly`; the runtime matrix covers AGP 8 and AGP 9. |

## Kotlin baseline

The next planned consumer Kotlin baseline is 2.2.0. It replaces 2.1.0 after the Kotlin 2.5 line is
validated as stable by the compatibility matrix.

The baseline may move earlier if a Kotlin 2.5 compatibility fix cannot be released while retaining
Kotlin 2.1 support. A required moko-resources release must not be blocked on a later Kotlin 2.5.x
patch solely to preserve the previous baseline.

## KotlinPoet

KotlinPoet 2.4.0 is intentionally deferred. Its explicit-backing-field, multi-field value-class,
and code-comment APIs are not used by the generator. KotlinPoet should be upgraded separately when
a required API or fix justifies the change.

See the [KotlinPoet 2.4.0 release notes](https://github.com/square/kotlinpoet/releases/tag/2.4.0).

## Android Gradle Plugin

The generator compiles against AGP 8.13.2 to remain on the AGP 8 DSL ABI while supporting both
AGP 8 and AGP 9 runtimes. Compiling against the AGP 9 API is a separate migration because it drops
AGP 8 binary compatibility.

AGP 8.13.2 and Gradle 8.14.2 are unrelated version lines. The latter is a Gradle distribution
version used by compatibility samples, not a newer AGP 8 release.

See the [AGP 8.13 release notes](https://developer.android.com/build/releases/agp-8-13-0-release-notes).

## Apple KLib resources: 0.28.0 breaking KLib format change

Starting with moko-resources 0.28.0, Apple KLibs with image or color resources
contain raw `Assets.xcassets` instead of a precompiled `Assets.car`. This makes
the KLib portable across Windows, Linux, and macOS, but moves asset catalog
compilation to the macOS project that links the final Apple framework or
executable.

This is a breaking change to the KLib resource format, not to the runtime API.
Gradle plugins are not inherited transitively from library dependencies, so the
project that links the final Apple binary must choose a compatible plugin
version itself.

| Produced Apple KLib | Final Apple consumer Gradle plugin | Result |
| --- | --- | --- |
| `< 0.28.0`, contains `Assets.car` | Any version | Supported |
| `>= 0.28.0`, no image/color resources | `< 0.28.0` | Supported; no asset catalog compilation is required |
| `>= 0.28.0`, contains raw `Assets.xcassets` | `< 0.28.0` | Not supported for images and colors; the raw catalog is copied but not compiled |
| `>= 0.28.0`, contains raw `Assets.xcassets` | `>= 0.28.0` | Supported; `actool` runs while linking the final Apple binary |

Newer plugin versions remain compatible with older KLibs that already contain
`Assets.car`.
