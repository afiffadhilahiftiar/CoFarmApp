plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // tambahkan ini

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
        manifestPlaceholders["mapbox_access_token"] = "pk.eyJ1IjoiYXBpcHZheHMiLCJhIjoiY21ieW1mZjF4MHE5ZDJqb2tydnY4ZWpzdCJ9.lKudAsGRB_XVCpEfxg9FXg"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true          // aktifkan shrinking & obfuscation
            isShrinkResources = true        // buang resource (gambar, layout) yang tidak terpakai
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs\\jtds-1.3.1.jar"))
    implementation(files("C:\\Users\\PC\\AndroidStudioProjects\\ContractFarmingApp\\app\\libs\\jtds-1.3.1.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    // Facebook Login
    implementation("com.facebook.android:facebook-android-sdk:latest.release")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // Material
    implementation("com.google.android.material:material:1.11.0")

    // GitHub
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("androidx.activity:activity-ktx:1.7.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //sql
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.android.volley:volley:1.2.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.mapbox.maps:android:11.13.1")

    implementation ("org.tensorflow:tensorflow-lite:2.13.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.3")

}
