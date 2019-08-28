//
//  Created by Aleksey Mikhailov on 23/06/2019.
//  Copyright © 2019 IceRock Development. All rights reserved.
//

import UIKit
import MultiPlatformLibrary

class TestViewController: UIViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        HelloWorldKt.helloWorld()
    }
}
