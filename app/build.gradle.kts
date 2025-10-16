plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase Integration
}

android {
    namespace = "com.example.contractfarmingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.contractfarmingapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Token Mapbox — gunakan placeholder, bukan token asli
        manifestPlaceholders["mapbox_access_token"] = "YOUR_MAPBOX_ACCESS_TOKEN"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true           // shrink & obfuscate code
            isShrinkResources = true         // remove unused resources
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
}

dependencies {

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase (gunakan BOM untuk versi otomatis)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")

    // Google Services
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:latest.release")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Background Tasks
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // QR Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Mapbox
    implementation("com.mapbox.maps:android:11.13.1")

    // TensorFlow (AI Detection)
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")

    // Local SQL Driver (pastikan file ada di libs/)
    implementation(files("libs/jtds-1.3.1.jar"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
