//
//  ContentView.swift
//  macos-app
//
//  Created by Nagy Robert on 06/12/2020.
//

import SwiftUI
import MultiPlatformLibrary

struct ContentView: View {
    
    let testing: Testing
    let image: NSImage
    let strings: [StringDesc]
    let textColor = MR.colors().textColor.color.toNSColor()
    
    init() {
//        StringDescCompanion.shared.localeType = StringDescLocaleType.Custom(locale: "ru")
        testing = Testing()
        image = testing.getDrawable().toNSImage()!
        strings = testing.getStrings()
        
        [
            testing.getTextFile(),
            testing.getJsonFile(),
            testing.getNestedJsonFile()
        ].map { $0.readText() }
        .forEach { print($0) }
    }
    
    
    var body: some View {
        VStack {
            Image(nsImage: image)
            
            Divider()
            
            Text(strings.map { $0.localized() }.joined(separator: "\n"))
                .font(Font(testing.getFontTtf1().nsFont(withSize: 14.0)))
                .foregroundColor(Color(textColor))
            
            Divider()
            
            Text(testing.getStringDesc().localized())
                .font(Font(testing.getFontTtf2().nsFont(withSize: 14.0)))
            
            Divider()
            
            if #available(macOS 12.0, *) {
                Text(testing.getPlurals().localized())
                    .textSelection(.enabled)
            } else {
                Text(testing.getPlurals().localized())
            }
        }
    }
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
