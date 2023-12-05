package cn.jailedbird.feature.about.expose

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Parcelize will be exposed in module_expose, so ensure add id("kotlin-parcelize") in build_gradle_template_expose
 * to make compile process successful
 * */
@Parcelize
data class AboutEntity(val title: String, val url: String) : Parcelable
