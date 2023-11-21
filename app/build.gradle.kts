plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.nowinandroid.android.room)
    // id("com.android.application")
    // id("org.jetbrains.kotlin.android")
    // id("com.google.devtools.ksp")
    // // id("kotlin-kapt") // remove kapt
    // id("com.google.dagger.hilt.android")
}

android {
    namespace = "cn.jailedbird.smartappsearch"
    buildFeatures {
        dataBinding {
            enable = true
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

// setupAppModule {
//     defaultConfig {
//         ndk {
//             abiFilters.add("arm64-v8a")
//             // abiFilters.add("armeabi-v7a")
//         }
//     }
//
//
//     enableViewBinding()
//     enableDataBinding()
// }

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.compose.runtime.tracing)
    // implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.window.manager)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.coil.kt)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.edgeutils)
    implementation(libs.recyclerview)
    // // Hilt https://developer.android.com/training/dependency-injection/hilt-android

    //
    // implementation(libs.lifecycle.viewmodel.ktx)
    // // replace SharedPreference:https://github.com/Tencent/MMKV
    // // implementation 'com.tencent:mmkv:1.2.14'
    //
    //
    // // https://square.github.io/leakcanary/getting_started/
    debugImplementation(libs.leakcanary)
    //
    // testImplementation(libs.junit)
    // androidTestImplementation(libs.ext.junit)
    // androidTestImplementation(libs.espresso.core)
}