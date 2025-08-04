plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

version = "0.2.2"

android {
    namespace = "com.ingonyama.imp1"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }

        ndk {
            abiFilters.add("arm64-v8a")
        }

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isJniDebuggable = true
            isPseudoLocalesEnabled = true
            isShrinkResources = false

            ndk.debugSymbolLevel = "FULL"

            externalNativeBuild.cmake {
                cppFlags += "-g"
                cppFlags += "-O0"
                arguments += "-DCMAKE_BUILD_TYPE=Debug"
            }

            packagingOptions.jniLibs.keepDebugSymbols.add("**/*.so")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        apiVersion = "1.9"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    libraryVariants.all {
        val variant = this
        outputs.all {
            val output = this
            if (output is com.android.build.gradle.internal.api.LibraryVariantOutputImpl) {
                output.outputFileName = if (variant.buildType.name == "release") {
                    "${rootProject.name}-${project.version}.aar"
                } else {
                    "${rootProject.name}-${project.version}-${variant.buildType.name}.aar"
                }
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
}