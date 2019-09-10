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

    spec.pod_target_xcconfig = {
        'MPP_LIBRARY_NAME' => 'MultiPlatformLibrary',
        'GRADLE_TASK[sdk=iphonesimulator*][config=*ebug]' => 'syncMultiPlatformLibraryDebugFrameworkIosX64',
        'GRADLE_TASK[sdk=iphonesimulator*][config=*elease]' => 'syncMultiPlatformLibraryReleaseFrameworkIosX64',
        'GRADLE_TASK[sdk=iphoneos*][config=*ebug]' => 'syncMultiPlatformLibraryDebugFrameworkIosArm64',
        'GRADLE_TASK[sdk=iphoneos*][config=*elease]' => 'syncMultiPlatformLibraryReleaseFrameworkIosArm64'
    }

    spec.script_phases = [
        {
            :name => 'Compile Kotlin/Native',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            #:output_files => ['$TARGET_BUILD_DIR/$PRODUCT_NAME.framework/$PRODUCT_NAME'],
            :script => <<-SCRIPT
MPP_PROJECT_ROOT="$SRCROOT/../../mpp-library"

MPP_OUTPUT_DIR="$MPP_PROJECT_ROOT/build/cocoapods/framework"
MPP_OUTPUT_NAME="$MPP_OUTPUT_DIR/#{spec.name}.framework"

"$MPP_PROJECT_ROOT/../gradlew" -p "$MPP_PROJECT_ROOT" "$GRADLE_TASK"
            SCRIPT
        }
    ]
end
