//
//  ContentView.swift
//  TestKotlinWatchApp
//
//  Created by Cornelli Fabio on 28/02/24.
//  Copyright © 2024 IceRock Development. All rights reserved.
//

import SwiftUI
import MppLibrary

struct ContentView: View {
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text(Testing.shared.getHelloWorld().localized())
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
