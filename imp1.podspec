Pod::Spec.new do |s|
  s.name             = 'IMP1'
  s.version          = '0.2.0'
  s.summary          = 'Mobile-first ZK proving framework powered by ICICLE'
  s.description      = <<-DESC
IMP1 is a lightweight, privacy-preserving zero-knowledge proving framework for iOS. Built on ICICLE-SNARK, optimized for mobile, and up to 3× faster than RapidSnark.
  DESC

  s.homepage         = 'https://github.com/ingonyama-zk/imp1'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Ingonyama' => 'hi@ingonyama.com' }
  s.source           = {
    :http => 'https://github.com/ingonyama-zk/imp1/releases/download/v0.2.0/imp1.xcframework.zip'
  }

  s.vendored_frameworks = 'imp1.xcframework'
  s.platform     = :ios, '13.0'
end
