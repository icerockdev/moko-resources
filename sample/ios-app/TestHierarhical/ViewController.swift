//
//  Created by Aleksey Mikhailov on 04.09.2020.
//  Copyright © 2020 IceRock Development. All rights reserved.
//

import UIKit
import mpp_hierarhical

class ViewController: UIViewController {

    @IBOutlet weak var text: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
      
        let string: StringResource = MR.strings().test_simple
        let stringDesc = string.desc()
        let outputString = stringDesc.localized()
        
        text.text = outputString
    }
}
