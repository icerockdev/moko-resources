/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import Foundation
import MultiPlatformLibrary

class StringFormatter: NSObject, StringDescFormatter {
  
  func formatString(string: String, args: KotlinArray<AnyObject>) -> String {
    
    var cargs = Array<CVarArg>()
    for iterator in 0...(args.size - 1) {
      let arg = args.get(index: iterator)
      switch arg {
      case let kotlinInt as KotlinInt:
        cargs.append(kotlinInt.intValue)
      case let float as Float:
        cargs.append(Double(float))
      case let kotlinDouble as KotlinDouble:
        cargs.append(kotlinDouble.doubleValue)
      case let cvarg as CVarArg:
        cargs.append(cvarg)
      default:
        continue
      }
    }
    return String(format: string, arguments: cargs)
  }
  
  func plural(resource: PluralsResource, number: Int32) -> String {
    let localized = NSLocalizedString(resource.resourceId, bundle: resource.bundle, comment: "")
    return String.localizedStringWithFormat(localized, number)
  }
  
  func formatPlural(resource: PluralsResource, number: Int32, args: KotlinArray<AnyObject>) -> String {
    let localized = NSLocalizedString(resource.resourceId, bundle: resource.bundle, comment: "")
    let pluralized = String.localizedStringWithFormat(localized, number)
    
    var cargs = Array<CVarArg>()
    for iterator in 0...(args.size - 1) {
      guard let arg = args.get(index: iterator) as? CVarArg else {
        continue
      }
      
      cargs.append(arg)
    }
    return String(format: pluralized, cargs)
  }
}

fileprivate let formatterInstance = StringFormatter()

public extension StringDesc {
  func localized() -> String {
    return toLocalizedString(formatter: formatterInstance)
  }
}
