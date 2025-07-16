plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ingonyama.imp1_aar_example"
    compileSdk = 36

    // Define asset pack names in one place
    val assetPackNames = listOf("zkey_pack_0", "zkey_pack_1", "zkey_pack_rarimo", "zkey_pack_zkp2p")

    defaultConfig {
        applicationId = "com.ingonyama.imp1_aar_example"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
        }

        // Expose asset pack names to runtime via BuildConfig
        buildConfigField("String[]", "ASSET_PACK_NAMES", "{${assetPackNames.joinToString(",") { "\"$it\"" }}}")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isPseudoLocalesEnabled = true
            isShrinkResources = false
            // Enable profiling
            isProfileable = true
            isDebuggable = false

            ndk.debugSymbolLevel = "FULL"

            packaging.jniLibs.keepDebugSymbols.add("**/*.so")
        }
    }

    buildFeatures {
        buildConfig = true
    }

//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }

    // Configure asset packs for zkey files using the defined names
    assetPacks += assetPackNames.map { ":$it" }
}

kotlin {
    jvmToolchain(8)
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation(files("libs/imp1-0.2.1.aar"))
    
    // Play Asset Delivery dependencies
    implementation("com.google.android.play:asset-delivery-ktx:2.3.0")
}