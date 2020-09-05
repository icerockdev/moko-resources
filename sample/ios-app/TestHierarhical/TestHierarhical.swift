//
//  Created by Aleksey Mikhailov on 04.09.2020.
//  Copyright © 2020 IceRock Development. All rights reserved.
//

import UIKit
import mpp_hierarhical

class TestHierarhical {
    
    static func test() {
        let string: StringResource = MR.strings().test_simple
        let stringDesc = string.desc()
        let outputString = stringDesc.localized()
        print("mpp_hierarhical \(outputString)")
    }
}

