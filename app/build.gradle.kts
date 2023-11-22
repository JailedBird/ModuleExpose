plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.flavors)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.nowinandroid.android.room)
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


dependencies {
    implementation(project(mapOf("path" to ":core:resource")))
    implementation(project(mapOf("path" to ":feature:settings")))
    implementation(project(mapOf("path" to ":feature:search")))
    implementation(project(mapOf("path" to ":core:settings")))

    // // Hilt https://developer.android.com/training/dependency-injection/hilt-android

    // // https://square.github.io/leakcanary/getting_started/
    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}