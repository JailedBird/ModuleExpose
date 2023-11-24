plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "cn.jailedbird.feature.about"

    buildFeatures {
        viewBinding = true
    }
}