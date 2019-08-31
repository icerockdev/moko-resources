//
//  Created by Aleksey Mikhailov on 23/06/2019.
//  Copyright © 2019 IceRock Development. All rights reserved.
//

import UIKit
import MultiPlatformLibrary
import MultiPlatformLibraryResources

class TestViewController: UIViewController {
    
    @IBOutlet private var imageView: UIImageView!
    @IBOutlet private var textView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let testing = Testing()
//        let drawable = testing.getDrawable()
        let strings = testing.getStrings()
        
//        imageView.image = UIImage(named: drawable.assetImageName)
        textView.text = strings.map { $0.localized() }.joined(separator: "\n")
    }
}
