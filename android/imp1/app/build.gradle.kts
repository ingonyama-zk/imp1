plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

version = "0.2.0"

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
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}