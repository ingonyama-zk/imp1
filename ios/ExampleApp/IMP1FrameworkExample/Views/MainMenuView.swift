import SwiftUI

struct MainMenuView: View {
    @State private var selectedExample: Example?
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Header
                VStack(spacing: 16) {
                    Text("Ingonyama")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                    
                    Text("IMP1 Example")
                        .font(.title2)
                        .padding(.top, 8)
                }
                .padding()
                .background(Color(.systemBackground))
                
                // Examples List
                List(ExampleManager.shared.examples) { example in
                    NavigationLink(destination: ProofGenerationView(example: example)) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(example.name)
                                .font(.headline)
                            Text(example.description)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
        }
        .onAppear(perform: handleViewAppear)
    }
    
    private func handleViewAppear() {
        print("MainMenuView appeared")
        print("Available examples:")
        for example in ExampleManager.shared.examples {
            print("- \(example.name): \(example.description)")
        }
    }
}

#Preview {
    MainMenuView()
} 