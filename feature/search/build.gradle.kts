plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "cn.jailedbird.feature.search"

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}


dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(projects.core.settings)
    implementation(projects.core.common)

    // implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    // implementation(libs.androidx.compose.runtime)
    // implementation(libs.androidx.lifecycle.runtimeCompose)
    // implementation(libs.androidx.compose.runtime.tracing)
    // implementation(libs.androidx.compose.material3.windowSizeClass)
    // implementation(libs.androidx.hilt.navigation.compose)
    // implementation(libs.androidx.navigation.compose)
    // implementation(libs.androidx.window.manager)
    // implementation(libs.androidx.profileinstaller)
    // implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.coil.kt)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.edgeutils)
    implementation(libs.recyclerview)
    compileOnly(projects.feature.settingsExpose)
    compileOnly(projects.feature.aboutExpose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}