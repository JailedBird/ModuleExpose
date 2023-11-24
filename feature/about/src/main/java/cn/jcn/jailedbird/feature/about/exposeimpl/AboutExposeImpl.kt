package cn.jcn.jailedbird.feature.about.exposeimpl

import androidx.appcompat.app.AppCompatActivity
import cn.jcn.jailedbird.feature.about.AboutActivity
import cn.jcn.jailedbird.feature.about.expose.AboutEntity
import cn.jcn.jailedbird.feature.about.expose.AboutExpose
import javax.inject.Inject

class AboutExposeImpl @Inject constructor() : AboutExpose {

    override fun openAboutActivity(starter: AppCompatActivity, aboutEntity: AboutEntity) {
        AboutActivity.start(starter, aboutEntity)
    }
}