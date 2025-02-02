import SwiftUI
import shared

struct ContentView: View {
    let greet = Greeting().getMR()

	var body: some View {
        Text(greet.localized())
            .foregroundStyle(Color(Extra.colors().textColor.getUIColor()))
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
