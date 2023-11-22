plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "cn.jailedbird.core.settings"
}

dependencies {
    implementation(projects.core.resource)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}