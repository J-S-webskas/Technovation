plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myappication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myappication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    packaging {
        resources {
            excludes += listOf(
                "**/kotlin/**",
                "**/*.kotlin_module",
                "META-INF/*.version",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )

            pickFirsts += listOf(
                "lib/**/libc++_shared.so",
                "lib/**/libmediapipe_tasks_vision_jni.so",
                "lib/**/libmediapipe_jni.so"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.camera.view)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // MediaPipe from TOML
//    implementation(libs.mediapipe.tasks.vision)
//    implementation(libs.mediapipe.tasks.core)
    implementation("com.google.mediapipe:tasks-vision:0.10.11")
    implementation("com.google.mediapipe:tasks-core:0.10.13")
    implementation("com.google.ai.edge.litert:litert-api:1.4.1")

    // MPAndroidChart from TOML
    implementation(libs.mpandroidchart)

    // CameraX from TOML
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
}