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
            name: "100k",
            directory: "100k",
            description: "A circuit with 100k constraints"
        ),
        Example(
            name: "200k",
            directory: "200k",
            description: "A circuit with 200k constraints"
        ),
            Example(
                name: "400k",
                directory: "400k",
            description: "A circuit with 400k constraints"
        ),
        Example(
            name: "800k",
            directory: "800k",
            description: "A circuit with 800k constraints"
        ),
        Example(
            name: "1600k",
            directory: "1600k",
            description: "A circuit with 1600k constraints"
        ),
        Example(
            name: "rarimo-bionet",
            directory: "rarimo-bionet",
            description: "rarimo-bionet"
        ),
        Example(
            name: "reclaim-aes-128-ctr",
            directory: "reclaim-aes-128-ctr",
            description: "reclaim-aes-128-ctr"
        ),
        Example(
            name: "reclaim-aes-256-ctr",
            directory: "reclaim-aes-256-ctr",
            description: "reclaim-aes-256-ctr"
        ),
        Example(
            name: "reclaim-chacha20",
            directory: "reclaim-chacha20",
            description: "reclaim-chacha20"
        ),
        Example(
            name: "Sha256",
            directory: "sha256",
            description: "Sha256"
        )
        
    ]
} 
