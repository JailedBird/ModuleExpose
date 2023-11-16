import com.yeahka.android.sgpos.enableDataBinding
import com.yeahka.android.sgpos.enableViewBinding
import com.yeahka.android.sgpos.setupAppModule

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    // id("kotlin-kapt") // remove kapt
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
    ksp(libs.room.compiler)
    implementation(libs.edgeutils)
    // Hilt https://developer.android.com/training/dependency-injection/hilt-android
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.lifecycle.viewmodel.ktx)
    // replace SharedPreference:https://github.com/Tencent/MMKV
    // implementation 'com.tencent:mmkv:1.2.14'


    // https://square.github.io/leakcanary/getting_started/
    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

//kapt {
//    correctErrorTypes = true
//}


hilt {
    enableAggregatingTask = true
}