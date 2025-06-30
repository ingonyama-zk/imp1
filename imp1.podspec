Pod::Spec.new do |s|
  s.name             = 'IMP1'
  s.version          = '0.2.0'
  s.summary          = 'Mobile-first ZK proving framework powered by ICICLE'
  s.description      = <<-DESC
IMP1 is a lightweight, privacy-preserving zero-knowledge proving framework for iOS. Built on ICICLE-SNARK, optimized for mobile, and up to 3× faster than RapidSnark.
  DESC

  s.homepage         = 'https://github.com/ingonyama-zk/imp1'
  s.license          = { type: 'MIT', file: 'LICENSE' }
  s.author           = { 'Ingonyama' => 'zk@ingonyama.com' }
  
  s.source           = { git: 'https://github.com/ingonyama-zk/imp1.git', tag: s.version.to_s }

  s.vendored_frameworks = 'imp1.xcframework'
  s.platform        = :ios
end
