Pod::Spec.new do |spec|
    spec.name                     = 'MultiPlatformLibraryResources'
    spec.version                  = '0.2.0'
    spec.homepage                 = 'https://github.com/icerockdev/moko-resources'
    spec.source                   = { :git => "https://github.com/icerockdev/moko-resources.git", :tag => "release/#{spec.version}" }
    spec.authors                  = 'IceRock Development'
    spec.license                  = { :type => 'Apache 2', :file => 'LICENSE.md' }
    spec.summary                  = 'Swift additions to moko-resources Kotlin/Native library'
    spec.module_name              = "#{spec.name}"
    
    spec.dependency 'MultiPlatformLibrary'
    spec.source_files = "resources/src/iosMain/swift/**/*.{h,m,swift}"

    spec.ios.deployment_target  = '9.0'
    spec.swift_version = '4.2'

    spec.pod_target_xcconfig = {
        'VALID_ARCHS' => '$(ARCHS_STANDARD_64_BIT)'
    }
end
