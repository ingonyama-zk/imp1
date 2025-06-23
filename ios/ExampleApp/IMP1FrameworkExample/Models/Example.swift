import Foundation

struct Example: Identifiable {
    let id = UUID()
    let name: String
    let directory: String
    let description: String
    
    var zkeyPath: String {
        return "Examples/\(directory)/circuit_final.zkey"
    }
    
    var witnessPath: String {
        return "Examples/\(directory)/witness.wtns"
    }
    
    // Paths in the app bundle
    var bundleZkeyPath: String {
        return "Examples/\(directory)/circuit_final.zkey"
    }
    
    var bundleWitnessPath: String {
        return "Examples/\(directory)/witness.wtns"
    }
    
    var bundleVerificationKeyPath: String {
        return "Examples/\(directory)/verification_key.json"
    }
}

class ExampleManager {
    static let shared = ExampleManager()
    
    private init() {}
    
    let examples: [Example] = [
        Example(
            name: "Sha256",
            directory: "sha256",
            description: "Sha256"
        )
        
    ]
} 
