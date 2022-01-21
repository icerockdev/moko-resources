Pod::Spec.new do |spec|
    spec.name                     = 'MultiPlatformLibrary'
    spec.version                  = '0.1.0'
    spec.homepage                 = 'Link to a Kotlin/Native module homepage'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'IceRock Development'
    spec.license                  = ''
    spec.summary                  = 'Shared code between iOS and Android'

    spec.vendored_frameworks      = "build/cocoapods/framework/#{spec.name}.framework"
    spec.libraries                = "c++"
    spec.module_name              = "#{spec.name}_umbrella"

    spec.ios.deployment_target = '11.0'
    spec.osx.deployment_target = '10.6'

    spec.pod_target_xcconfig = {
        'KOTLIN_FRAMEWORK_BUILD_TYPE[config=*ebug]' => 'debug',
        'KOTLIN_FRAMEWORK_BUILD_TYPE[config=*elease]' => 'release',
        'CURENT_SDK[sdk=iphoneos*]' => 'iphoneos',
        'CURENT_SDK[sdk=iphonesimulator*]' => 'iphonesimulator',
        'CURENT_SDK[sdk=macosx*]' => 'macos'
    }

    spec.script_phases = [
        {
            :name => 'Compile Kotlin/Native',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
if [ "$KOTLIN_FRAMEWORK_BUILD_TYPE" == "debug" ]; then
  CONFIG="Debug"
else
  CONFIG="Release"
fi

if [ "$CURENT_SDK" == "iphoneos" ]; then
  TARGET="Ios"
  ARCH="Arm64"
elif [ "$CURENT_SDK" == "macos" ]; then
  TARGET="Macos"
  if [ "$NATIVE_ARCH" == "arm64" ]; then
    ARCH="Arm64"
  else
    ARCH="X64"
  fi
else
  if [ "$NATIVE_ARCH" == "arm64" ]; then
    TARGET="IosSimulator"
    ARCH="Arm64"
  else
    TARGET="Ios"
    ARCH="X64"
  fi
fi

MPP_PROJECT_ROOT="$SRCROOT/../../mpp-library"
GRADLE_TASK="syncMultiPlatformLibrary${CONFIG}Framework${TARGET}${ARCH}"

"$MPP_PROJECT_ROOT/../gradlew" -p "$MPP_PROJECT_ROOT" "$GRADLE_TASK"
            SCRIPT
        }
    ]
end
