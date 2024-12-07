// clang -target arm64-apple-ios -isysroot $(xcrun --sdk iphoneos --show-sdk-path) -c MRResourcesBundle.m -o source.o
// ar rcs libMRResourcesBundle.a source.o
// lipo -info libMRResourcesBundle.a

#import <stdarg.h>
#import <Foundation/NSBundle.h>
#import "MRResourcesBundle.h"

@implementation ResourcesBundleAnchor

+ (NSBundle*) getResourcesBundle {
    return [NSBundle bundleForClass:[ResourcesBundleAnchor class]];
}

@end
