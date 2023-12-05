package cn.jailedbird.feature.about.expose

import androidx.appcompat.app.AppCompatActivity
import cn.jailedbird.feature.about.expose.AboutEntity

interface AboutExpose {
    /**
     * Ensure start as AppCompatActivity, rather than Context, so please implementation(libs.androidx.appcompat)
     * in build_gradle_template_expose
     * */
    fun openAboutActivity(starter: AppCompatActivity, aboutEntity: AboutEntity)
}