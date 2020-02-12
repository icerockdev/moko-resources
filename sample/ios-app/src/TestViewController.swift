//
//  Created by Aleksey Mikhailov on 23/06/2019.
//  Copyright © 2019 IceRock Development. All rights reserved.
//

import UIKit
import MultiPlatformLibrary

class TestViewController: UIViewController {
    
    @IBOutlet private var imageView: UIImageView!
    @IBOutlet private var textView: UITextView!
    @IBOutlet private var stringDescTextView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let testing = Testing()
        let drawable = testing.getDrawable()
        let strings = testing.getStrings()
        
        imageView.image = drawable.toUIImage()
        textView.text = strings.map { $0.localized() }.joined(separator: "\n")
        textView.font = testing.getFont1().uiFont(withSize: 14.0)
        
        stringDescTextView.text = testing.getStringDesc().localized()
        stringDescTextView.font = testing.getFont2().uiFont(withSize: 14.0)
    }
}

class LanguageTableViewController: UITableViewController {
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if(segue.identifier == "") {
            Testing().locale(lang: nil)
        } else {
            Testing().locale(lang: segue.identifier)
        }
    }
}
