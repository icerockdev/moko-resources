/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.icerockdev.library.Testing
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCOutlet
import platform.Foundation.NSCoder
import platform.UIKit.UILabel
import platform.UIKit.UIViewController
import platform.UIKit.UIViewControllerMeta

@ExportObjCClass
class LabelViewController : UIViewController {
    companion object Meta : UIViewControllerMeta()

    @ObjCOutlet
    lateinit var resourcesLabel: UILabel

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    override fun viewDidLoad() {
        super.viewDidLoad()
        resourcesLabel.text = Testing.getStringDesc().localized()
    }
}
