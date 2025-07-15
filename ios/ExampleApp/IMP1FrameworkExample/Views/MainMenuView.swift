import SwiftUI

struct MainMenuView: View {
    @State private var isParallelMode = false
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
                
                // Proof Mode Toggle
                VStack(spacing: 12) {
                    Text("Proof Generation Mode")
                        .font(.headline)
                        .padding(.top, 8)
                    
                    HStack(spacing: 20) {
                        Button("Single Proof") {
                            isParallelMode = false
                            print("Switched to Single Proof mode")
                        }
                        .font(.subheadline)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(isParallelMode ? Color.gray : Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        
                        Button("Parallel Proofs") {
                            isParallelMode = true
                            print("Switched to Parallel Proofs mode")
                        }
                        .font(.subheadline)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(isParallelMode ? Color.green : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                    
                    Text("Current Mode: \(isParallelMode ? "Parallel Proofs" : "Single Proof")")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .padding(.bottom, 8)
                }
                .padding()
                .background(Color(.systemGray6))
                
                // Examples List
                List(ExampleManager.shared.examples) { example in
                    Button(action: {
                        print("Example selected: \(example.name) in \(isParallelMode ? "parallel" : "single") mode")
                        selectedExample = example
                    }) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(example.name)
                                .font(.headline)
                                .foregroundColor(.primary)
                            Text(example.description)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .padding(.vertical, 4)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
        }
        .sheet(item: $selectedExample) { example in
            if isParallelMode {
                ParallelProofView(example: example)
            } else {
                ProofGenerationView(example: example)
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