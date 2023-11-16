import com.yeahka.android.sgpos.enableDataBinding
import com.yeahka.android.sgpos.enableViewBinding
import com.yeahka.android.sgpos.setupAppModule

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}
setupAppModule {
    defaultConfig {
        ndk {
            abiFilters.add("arm64-v8a")
            // abiFilters.add("armeabi-v7a")
        }
    }

    enableViewBinding()
    enableDataBinding()
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.activity.ktx)
    implementation(libs.coil)
    implementation(libs.material)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    val room_version = "2.4.3"
    ksp("androidx.room:room-compiler:$room_version")
    // implementation project(":lib")
    implementation(libs.edgeutils)
    // Hilt https://developer.android.com/training/dependency-injection/hilt-android
    val hilt = "2.48.1"
    implementation("com.google.dagger:hilt-android:$hilt")
    ksp("com.google.dagger:hilt-compiler:$hilt")

    implementation(libs.lifecycle.viewmodel.ktx)
    // replace SharedPreference:https://github.com/Tencent/MMKV
    // implementation 'com.tencent:mmkv:1.2.14'


    // https://square.github.io/leakcanary/getting_started/
    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

kapt {
    correctErrorTypes = true
}


hilt {
    enableAggregatingTask = true
}