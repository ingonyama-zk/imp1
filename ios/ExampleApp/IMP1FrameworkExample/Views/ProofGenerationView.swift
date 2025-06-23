import SwiftUI
import imp1

struct ProofGenerationView: View {
    let ERROR_SIZE = UInt(256)
    let example: Example
    @State private var proofResult: String = "No proof generated yet"
    @Environment(\.dismiss) private var dismiss
    
    init(example: Example) {
        print("ProofGenerationView initializing with example: \(example.name)")
        self.example = example
    }
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Proof Generation - \(example.name)")
                .font(.title)
                .padding()
            
            ScrollView {
                Text(proofResult)
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .textSelection(.enabled)  // Allow text selection
            }
            .frame(height: 300)  // Fixed height to show about 10 lines
            .background(Color(.systemBackground))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.white, lineWidth: 2)
            )
            .padding(.horizontal)
            
            Button("Generate Proof") {
                generateProof()
            }
            .buttonStyle(.borderedProminent)
            
            Button("Back to Examples") {
                dismiss()
            }
            .buttonStyle(.bordered)
        }
        .onAppear {
            print("ProofGenerationView body appeared")
            print("Example details:")
            print("- Name: \(example.name)")
            print("- Directory: \(example.directory)")
            print("- ZKey path: \(example.bundleZkeyPath)")
            print("- Witness path: \(example.bundleWitnessPath)")
            print("- Verification key path: \(example.bundleVerificationKeyPath)")
            
            // Check if files exist in bundle
            if let zkeyPath = Bundle.main.path(forResource: example.bundleZkeyPath, ofType: nil) {
                print("Found zkey file at: \(zkeyPath)")
            } else {
                print("Could not find zkey file at: \(example.bundleZkeyPath)")
            }
            
            if let witnessPath = Bundle.main.path(forResource: example.bundleWitnessPath, ofType: nil) {
                print("Found witness file at: \(witnessPath)")
            } else {
                print("Could not find witness file at: \(example.bundleWitnessPath)")
            }
            
            if let verificationKeyPath = Bundle.main.path(forResource: example.bundleVerificationKeyPath, ofType: nil) {
                print("Found verification key file at: \(verificationKeyPath)")
            } else {
                print("Could not find verification key file at: \(example.bundleVerificationKeyPath)")
            }
        }
    }
    
    private func getDocumentsDirectory() -> String {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return paths[0].path
    }
    
    private func withCStrings<T>(_ strings: [String], _ body: ([UnsafePointer<CChar>]) -> T) -> T {
        let cStrings = strings.map { str -> UnsafePointer<CChar> in
            let cStr = strdup(str)!
            return UnsafePointer(cStr)
        }
        
        let result = body(cStrings)
        
        // Clean up the C strings we allocated with strdup
        cStrings.forEach { ptr in
            free(UnsafeMutableRawPointer(mutating: ptr))
        }
        
        return result
    }
    
    private func generateProof() {
        print("Starting proof generation for \(example.name)")
        
        // Get paths for input files from bundle
        guard let bundleZkeyPath = Bundle.main.path(forResource: example.bundleZkeyPath, ofType: nil) else {
            proofResult = "Error: Could not find zkey file"
            return
        }
        guard let bundleWitnessPath = Bundle.main.path(forResource: example.bundleWitnessPath, ofType: nil) else {
            proofResult = "Error: Could not find witness file"
            return
        }
        guard let bundleVerificationKeyPath = Bundle.main.path(forResource: example.bundleVerificationKeyPath, ofType: nil) else {
            proofResult = "Error: Could not find verification key file"
            return
        }
        let documentsPath = getDocumentsDirectory()
        let exampleDir = (documentsPath as NSString).appendingPathComponent(example.directory)
        let fileManager = FileManager.default
        if !fileManager.fileExists(atPath: exampleDir) {
            do {
                try fileManager.createDirectory(atPath: exampleDir, withIntermediateDirectories: true)
            } catch {
                proofResult = "Error creating directory: \(error.localizedDescription)"
                return
            }
        }
        let documentsZkeyPath = (exampleDir as NSString).appendingPathComponent("circuit_final.zkey")
        let documentsWitnessPath = (exampleDir as NSString).appendingPathComponent("witness.wtns")
        let documentsVerificationKeyPath = (exampleDir as NSString).appendingPathComponent("verification_key.json")
        let proofPath = (exampleDir as NSString).appendingPathComponent("proof.json")
        let publicPath = (exampleDir as NSString).appendingPathComponent("public.json")
        let device = DeviceType.CpuMetal
        do {
            if fileManager.fileExists(atPath: documentsZkeyPath) {
                try fileManager.removeItem(atPath: documentsZkeyPath)
            }
            if fileManager.fileExists(atPath: documentsWitnessPath) {
                try fileManager.removeItem(atPath: documentsWitnessPath)
            }
            if fileManager.fileExists(atPath: documentsVerificationKeyPath) {
                try fileManager.removeItem(atPath: documentsVerificationKeyPath)
            }
            try fileManager.copyItem(atPath: bundleZkeyPath, toPath: documentsZkeyPath)
            try fileManager.copyItem(atPath: bundleWitnessPath, toPath: documentsWitnessPath)
            try fileManager.copyItem(atPath: bundleVerificationKeyPath, toPath: documentsVerificationKeyPath)
//            setenv("TMPDIR", exampleDir, 1)
            
            let errorBuffer = UnsafeMutablePointer<UInt8>.allocate(capacity: Int(ERROR_SIZE))
            let errorBufferSize = UInt64(ERROR_SIZE)

            let proof_result = withCStrings([documentsWitnessPath, documentsZkeyPath, proofPath, publicPath]) { ptrs in
                let witnessPathPtr = ptrs[0]
                let zkeyPathPtr = ptrs[1]
                let proofPathPtr = ptrs[2]
                let publicPathPtr = ptrs[3]
                return prove(witnessPathPtr, zkeyPathPtr, proofPathPtr, publicPathPtr, errorBuffer, errorBufferSize, device)
            }

            
            if proof_result == .ProverSuccess {
                let verify_result: VerifierResult = withCStrings([proofPath, publicPath, documentsVerificationKeyPath]) { ptrs in
                    let proofPathPtr = ptrs[0]
                    let publicPathPtr = ptrs[1]
                    let vkPathPtr = ptrs[2]
                    
                    return verify(proofPathPtr, publicPathPtr, vkPathPtr)
                }

                if verify_result == .success {
                    proofResult = "Groth16 proof generated and verified successfully!"
                } else {
                    proofResult = "Groth16 proof generated but verified failed!"
                }
            } else {
                proofResult = "Groth16 failed: \(String(cString: errorBuffer))"
            }
            errorBuffer.deallocate()
        } catch {
            proofResult = "Error copying files: \(error.localizedDescription)"
        }
        print("Proof finished")
    }
}

#Preview {
    ProofGenerationView(example: ExampleManager.shared.examples[0])
} 
