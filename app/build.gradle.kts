plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.flavors)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.nowinandroid.android.room)
}

android {
    namespace = "cn.jailedbird.app"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.core.resource)
    implementation(projects.core.common)
    implementation(projects.core.settings)
    implementation(projects.feature.search)
    implementation(projects.feature.settings)
    implementation(projects.feature.about)

    // // Hilt https://developer.android.com/training/dependency-injection/hilt-android

    // // https://square.github.io/leakcanary/getting_started/
    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}