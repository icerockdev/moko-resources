//
//  Created by Aleksey Mikhailov on 23/06/2019.
//  Copyright © 2019 IceRock Development. All rights reserved.
//

import UIKit
import MultiPlatformLibrary

class TestViewController: UIViewController {

    @IBOutlet private var imageView: UIImageView!
    @IBOutlet private var svgImageView: UIImageView!
    @IBOutlet private var textView: UITextView!
    @IBOutlet private var stringDescTextView: UITextView!

    override func viewDidLoad() {
        super.viewDidLoad()

        let testing = Testing()
        let drawable = testing.getDrawable()
        let vectorDrawable = testing.getVectorDrawable()
        let strings = testing.getStrings()

        imageView.image = drawable.toUIImage()

        svgImageView.image = vectorDrawable.toUIImage()

        let textColor: UIColor = MR.colors().textColor.getUIColor()
        textView.text = strings.map { $0.localized() }.joined(separator: "\n")
        textView.font = testing.getFontTtf1().uiFont(withSize: 14.0)
        textView.textColor = textColor

        stringDescTextView.text = testing.getStringDesc().localized()
        stringDescTextView.font = testing.getFontTtf2().uiFont(withSize: 14.0)

        [
            testing.getTextFile(),
            testing.getJsonFile(),
            testing.getNestedJsonFile()
        ].map { $0.readText() }
            .forEach { print($0) }

        testing.getTextsFromAssets().map { $0.readText() }
                .forEach { print($0) }
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
