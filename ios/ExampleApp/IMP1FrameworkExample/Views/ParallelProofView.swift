import SwiftUI
import imp1

struct ParallelProofView: View {
    let ERROR_SIZE = UInt(256)
    let example: Example
    @State private var proofResults: [String] = []
    @State private var verificationResults: [String] = []
    @State private var proofRuntime: String = ""
    @State private var verificationRuntime: String = ""
    @State private var numParallelProofs: Int = 2
    @State private var isGenerating = false
    @Environment(\.dismiss) private var dismiss
    
    init(example: Example) {
        print("ParallelProofView initializing with example: \(example.name)")
        self.example = example
    }
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Parallel Proof Generation - \(example.name)")
                .font(.title)
                .padding()
            
            // Configuration
            VStack(alignment: .leading, spacing: 10) {
                Text("Number of parallel proofs:")
                    .font(.headline)
                
                Stepper(value: $numParallelProofs, in: 1...50) {
                    Text("\(numParallelProofs) proofs")
                        .font(.subheadline)
                }
                .padding(.horizontal)
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(8)
            
            // Results
            ScrollView {
                VStack(alignment: .leading, spacing: 10) {
                    if proofResults.isEmpty {
                        Text("No proofs generated yet")
                            .foregroundColor(.secondary)
                            .padding()
                    } else {
                        ForEach(Array(proofResults.enumerated()), id: \.offset) { index, result in
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Proof \(index + 1):")
                                    .font(.headline)
                                    .foregroundColor(.blue)
                                Text(result)
                                    .font(.body)
                                
                                // Show verification result if available
                                if index < verificationResults.count {
                                    Text(verificationResults[index])
                                        .font(.body)
                                        .foregroundColor(verificationResults[index].contains("✅") ? .green : .red)
                                }
                            }
                            .padding()
                            .background(Color(.systemBackground))
                            .cornerRadius(8)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                            )
                        }
                    }
                    
                    if !proofRuntime.isEmpty {
                        Text("Proof Generation Runtime: \(proofRuntime)")
                            .font(.headline)
                            .foregroundColor(.green)
                            .padding(.horizontal)
                    }
                    
                    if !verificationRuntime.isEmpty {
                        Text("Verification Runtime: \(verificationRuntime)")
                            .font(.headline)
                            .foregroundColor(.orange)
                            .padding(.horizontal)
                    }
                }
            }
            .frame(height: 400)
            .background(Color(.systemBackground))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.white, lineWidth: 2)
            )
            .padding(.horizontal)
            
            // Buttons
            VStack(spacing: 10) {
                Button(isGenerating ? "Processing..." : "Generate & Verify Parallel Proofs") {
                    generateParallelProofs()
                }
                .buttonStyle(.borderedProminent)
                .disabled(isGenerating)
                
                Button("Back to Examples") {
                    dismiss()
                }
                .buttonStyle(.bordered)
            }
        }
        .onAppear {
            print("ParallelProofView body appeared")
        }
    }
    
    private func getDocumentsDirectory() -> String {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return paths[0].path
    }
    

    
    private func formatTimeInterval(_ timeInterval: TimeInterval) -> String {
        if timeInterval < 1.0 {
            return String(format: "%.3f seconds", timeInterval)
        } else if timeInterval < 60.0 {
            return String(format: "%.2f seconds", timeInterval)
        } else {
            let minutes = Int(timeInterval / 60)
            let seconds = timeInterval.truncatingRemainder(dividingBy: 60)
            return String(format: "%d minutes %.2f seconds", minutes, seconds)
        }
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
    
    private func generateParallelProofs() {
        print("Starting parallel proof generation for \(example.name) with \(numParallelProofs) proofs")
        
        isGenerating = true
        proofResults = []
        verificationResults = []
        proofRuntime = ""
        verificationRuntime = ""
        
        // Get paths for input files from bundle
        guard let bundleZkeyPath = Bundle.main.path(forResource: example.bundleZkeyPath, ofType: nil) else {
            proofResults = ["Error: Could not find zkey file"]
            isGenerating = false
            return
        }
        guard let bundleWitnessPath = Bundle.main.path(forResource: example.bundleWitnessPath, ofType: nil) else {
            proofResults = ["Error: Could not find witness file"]
            isGenerating = false
            return
        }
        
        let documentsPath = getDocumentsDirectory()
        let exampleDir = (documentsPath as NSString).appendingPathComponent(example.directory)
        let fileManager = FileManager.default
        
        // Create directory if it doesn't exist
        if !fileManager.fileExists(atPath: exampleDir) {
            do {
                try fileManager.createDirectory(atPath: exampleDir, withIntermediateDirectories: true)
            } catch {
                proofResults = ["Error creating directory: \(error.localizedDescription)"]
                isGenerating = false
                return
            }
        }
        
        // Prepare arrays for parallel proof generation
        var witnessPaths: [String] = []
        var proofPaths: [String] = []
        var publicPaths: [String] = []
        
        // Use single zkey file for all proofs
        let documentsZkeyPath = (exampleDir as NSString).appendingPathComponent("circuit_final.zkey")
        
        // Copy zkey file once
        do {
            if fileManager.fileExists(atPath: documentsZkeyPath) {
                try fileManager.removeItem(atPath: documentsZkeyPath)
            }
            try fileManager.copyItem(atPath: bundleZkeyPath, toPath: documentsZkeyPath)
        } catch {
            proofResults = ["Error copying zkey file: \(error.localizedDescription)"]
            isGenerating = false
            return
        }
        
        for i in 0..<numParallelProofs {
            let documentsWitnessPath = (exampleDir as NSString).appendingPathComponent("witness_\(i).wtns")
            let proofPath = (exampleDir as NSString).appendingPathComponent("proof_\(i).json")
            let publicPath = (exampleDir as NSString).appendingPathComponent("public_\(i).json")
            
            // Copy witness file for this proof instance
            do {
                if fileManager.fileExists(atPath: documentsWitnessPath) {
                    try fileManager.removeItem(atPath: documentsWitnessPath)
                }
                try fileManager.copyItem(atPath: bundleWitnessPath, toPath: documentsWitnessPath)
            } catch {
                proofResults = ["Error copying witness file for proof \(i + 1): \(error.localizedDescription)"]
                isGenerating = false
                return
            }
            
            witnessPaths.append(documentsWitnessPath)
            proofPaths.append(proofPath)
            publicPaths.append(publicPath)
        }
        
        let device = DeviceType.CpuMetal
        let errorBuffer = UnsafeMutablePointer<UInt8>.allocate(capacity: Int(ERROR_SIZE))
        let errorBufferSize = UInt64(ERROR_SIZE)
        
        // Start timing the parallel_prove() function
        let startTime = CFAbsoluteTimeGetCurrent()
        
        // Create C strings for all paths
        let witnessPathPtrs = witnessPaths.map { strdup($0)! }
        let proofPathPtrs = proofPaths.map { strdup($0)! }
        let publicPathPtrs = publicPaths.map { strdup($0)! }
        let zkeyPathCStr = strdup(documentsZkeyPath)!
        
        // Convert to arrays of optional pointers as expected by the C function
        var witnessPathPtrsArray = witnessPathPtrs.map { UnsafePointer<CChar>($0) as UnsafePointer<CChar>? }
        var proofPathPtrsArray = proofPathPtrs.map { UnsafePointer<CChar>($0) as UnsafePointer<CChar>? }
        var publicPathPtrsArray = publicPathPtrs.map { UnsafePointer<CChar>($0) as UnsafePointer<CChar>? }
        
        // Get mutable pointers to the arrays
        let witnessPathPtrsPtr = witnessPathPtrsArray.withUnsafeMutableBufferPointer { $0.baseAddress }
        let proofPathPtrsPtr = proofPathPtrsArray.withUnsafeMutableBufferPointer { $0.baseAddress }
        let publicPathPtrsPtr = publicPathPtrsArray.withUnsafeMutableBufferPointer { $0.baseAddress }
        
        let results_ptr = parallel_prove(
            witnessPathPtrsPtr,
            UnsafePointer<CChar>(zkeyPathCStr),
            proofPathPtrsPtr,
            publicPathPtrsPtr,
            UInt64(numParallelProofs),
            errorBuffer,
            errorBufferSize,
            device,
            0  // Use default max_batch_size of 10
        )
        
        // Clean up all allocated C strings
        witnessPathPtrs.forEach { free($0) }
        proofPathPtrs.forEach { free($0) }
        publicPathPtrs.forEach { free($0) }
        free(zkeyPathCStr)
        
        // End timing and calculate runtime
        let endTime = CFAbsoluteTimeGetCurrent()
        let runtime = endTime - startTime
        proofRuntime = formatTimeInterval(runtime)
        
        print("Parallel proof generation completed in \(proofRuntime)")
        
        // Process results
        if let results_ptr = results_ptr {
            print("Swift: Got results_ptr, creating array with count: \(numParallelProofs)")
            let buffer = UnsafeBufferPointer(start: results_ptr, count: numParallelProofs)
            let rawPtr = UnsafeRawPointer(results_ptr)
            let byteCount = numParallelProofs * MemoryLayout<Int64>.stride
            let bytes = rawPtr.bindMemory(to: UInt8.self, capacity: byteCount)
            print("Swift: Raw bytes of results array:", (0..<byteCount).map { bytes[$0] })
            print("Swift: MemoryLayout<Int64>.stride = \(MemoryLayout<Int64>.stride)")
            print("Swift: MemoryLayout<Int64>.size = \(MemoryLayout<Int64>.size)")
            print("Swift: MemoryLayout<Int64>.alignment = \(MemoryLayout<Int64>.alignment)")
            let results = Array(buffer)
            print("Swift: Created results array with \(results.count) elements")
            
            for (index, result) in results.enumerated() {
                print("Swift: Processing result \(index + 1): value=\(result)")
                if result == 0 {
                    print("Swift: Result \(index + 1) is Success (0)")
                    proofResults.append("✅ Success")
                } else if result == 1 {
                    print("Swift: Result \(index + 1) is Failure (1)")
                    proofResults.append("❌ Failed")
                } else {
                    print("Swift: Result \(index + 1) is unknown value: \(result)")
                    proofResults.append("❓ Unknown result")
                }
            }
            
            // Free the results array
            free_parallel_results(results_ptr, UInt64(numParallelProofs))
        } else {
            let errorMessage = String(cString: errorBuffer)
            proofResults = ["Error: \(errorMessage)"]
        }
        
        errorBuffer.deallocate()
        
        // Now verify all generated proofs
        print("Starting verification of \(numParallelProofs) proofs")
        verificationResults = []
        
        // Get verification key path
        guard let bundleVerificationKeyPath = Bundle.main.path(forResource: example.bundleVerificationKeyPath, ofType: nil) else {
            verificationResults = ["Error: Could not find verification key file"]
            isGenerating = false
            return
        }
        
        let documentsVerificationKeyPath = (exampleDir as NSString).appendingPathComponent("verification_key.json")
        
        // Copy verification key file
        do {
            if fileManager.fileExists(atPath: documentsVerificationKeyPath) {
                try fileManager.removeItem(atPath: documentsVerificationKeyPath)
            }
            try fileManager.copyItem(atPath: bundleVerificationKeyPath, toPath: documentsVerificationKeyPath)
        } catch {
            verificationResults = ["Error copying verification key file: \(error.localizedDescription)"]
            isGenerating = false
            return
        }
        
        // Start timing verification
        let verifyStartTime = CFAbsoluteTimeGetCurrent()
        
        // Verify each proof individually
        for i in 0..<numParallelProofs {
            let proofPath = proofPaths[i]
            let publicPath = publicPaths[i]
            
            print("Verifying proof \(i + 1): \(proofPath)")
            
            let verifyResult: VerifierResult = withCStrings([proofPath, publicPath, documentsVerificationKeyPath]) { ptrs in
                let proofPathPtr = ptrs[0]
                let publicPathPtr = ptrs[1]
                let vkPathPtr = ptrs[2]
                return verify(proofPathPtr, publicPathPtr, vkPathPtr)
            }
            
            if verifyResult == .success {
                print("Proof \(i + 1) verified successfully")
                verificationResults.append("✅ Verified")
            } else {
                print("Proof \(i + 1) verification failed")
                verificationResults.append("❌ Failed")
            }
        }
        
        // End timing verification
        let verifyEndTime = CFAbsoluteTimeGetCurrent()
        let verifyRuntime = verifyEndTime - verifyStartTime
        verificationRuntime = formatTimeInterval(verifyRuntime)
        
        print("Verification completed in \(verificationRuntime)")
        
        isGenerating = false
        print("Parallel proof generation and verification finished")
    }
}

#Preview {
    ParallelProofView(example: ExampleManager.shared.examples[0])
} 