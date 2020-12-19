//
//  TestHierarhical.swift
//  TestHierarhical
//
//  Created by Nagy Robert on 07/12/2020.
//

import Foundation
import mpp_hierarhical

class TestHierarhical {
    
    static func test() {
        let string: StringResource = MR.strings().test_simple
        let stringDesc = string.desc()
        let outputString = stringDesc.localized()
        print("mpp_hierarhical \(outputString)")
    }
}

