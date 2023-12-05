package cn.jailedbird.feature.about.exposeimpl

import androidx.appcompat.app.AppCompatActivity
import cn.jailedbird.feature.about.AboutActivity
import cn.jailedbird.feature.about.expose.AboutEntity
import cn.jailedbird.feature.about.expose.AboutExpose
import javax.inject.Inject

class AboutExposeImpl @Inject constructor() : AboutExpose {

    /**
     * @param starter 传递context即可 但是这里为了演示 build_gradle_template_expose 自定义依赖的功能，特意如此设置
     * @param aboutEntity AboutEntity需要模块启用 id("kotlin-parcelize") 默认模板也无法做法 同样需要 build_gradle_template_expose实现自定义
     * */
    override fun openAboutActivity(starter: AppCompatActivity, aboutEntity: AboutEntity) {
        AboutActivity.start(starter, aboutEntity)
    }
}